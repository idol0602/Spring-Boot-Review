package nguyennhatquan.springbootreview.shared.service.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class MultiLevelCacheService {

    private final Cache<String, Object> caffeineCache;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheMessagePublisher cacheMessagePublisher;
    private final ObjectMapper objectMapper;

    @Value("${app.cache.redis.ttl-minutes:60}")
    private long redisTtlMinutes;

    public <T> T get(String key, Supplier<T> dbFetcher, TypeReference<T> typeReference) {
        Object cachedValue = caffeineCache.getIfPresent(key);
        if (cachedValue != null) {
            log.info("L1 Cache hit: {}", key);
            return objectMapper.convertValue(cachedValue, typeReference);
        }

        Object redisValue = redisTemplate.opsForValue().get(key);
        if (redisValue != null) {
            log.info("L2 Cache hit: {}", key);
            T response = objectMapper.convertValue(redisValue, typeReference);
            caffeineCache.put(key, response);
            return response;
        }

        T response = dbFetcher.get();

        caffeineCache.put(key, response);
        redisTemplate.opsForValue().set(key, response, redisTtlMinutes, TimeUnit.MINUTES);

        return response;
    }

    public void evict(String entityName, String id) {
        String key = entityName + "_" + id;
        caffeineCache.invalidate(key);
        redisTemplate.delete(key);
        cacheMessagePublisher.publishCacheInvalidation(entityName, id, "DELETE");
    }

    public void evictPrefix(String prefix) {
        caffeineCache.asMap().keySet().removeIf(k -> k.startsWith(prefix));
        Set<String> keys = redisTemplate.keys(prefix + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        cacheMessagePublisher.publishCacheInvalidation(prefix, "*", "DELETE_PREFIX");
    }
}
