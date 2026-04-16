package nguyennhatquan.springbootreview.shared.service.cache;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nguyennhatquan.springbootreview.shared.dto.CacheSyncEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheMessageListener {
    private final Cache<String,Object> caffeineCache;

    @RabbitListener(queues = "#{autoDeleteCacheQueue.name}")
    public void receiveCacheSyncMessage(CacheSyncEvent event) {
        if ("DELETE_PREFIX".equals(event.getAction())) {
            String prefix = event.getEntityName();
            caffeineCache.asMap().keySet().removeIf(k -> k.startsWith(prefix));
            log.info("🧹 Đã dọn RAM bằng lệnh RabbitMQ, theo prefix: {}", prefix);
        } else if(event.getEntityName() != null && event.getEntityId() != null) {
            String key = event.getEntityName().toLowerCase() + "_" + event.getEntityId();
            caffeineCache.invalidate(key);
            log.info("🧹 Đã dọn RAM bằng lệnh RabbitMQ, key: {}", key);
        }
    }
}
