# Hướng Dẫn Tự Code: Multi-Level Cache (Hot - Warm - Cold) Bằng Caffeine, Redis, và RabbitMQ

Tài liệu này hướng dẫn chi tiết các bước thiết lập kiến trúc cache 3 tầng để bạn có thể tự mình xây dựng lại trong dự án Spring Boot.

## 1. Bản Chất Của Kiến Trúc
Ở các hệ thống quy mô vừa đến lớn, ta chia Cache ra làm các tầng:
- **Hot Cache (L1):** Đặt trực tiếp trong RAM của ứng dụng Boot. Rất nhanh (micro/nano-s), nhưng nhược điểm là tốn RAM server và xảy ra hiện tượng **bất đồng bộ (stale data)** nếu chạy nhiều instances (load balancing). Chúng ta dùng `Caffeine`.
- **Warm Cache (L2):** Đặt ở Server tập trung ngoài. Tốc độ chậm hơn (mili-s) do tốn thời gian gọi mạng, nhưng mọi server Boot đều lấy được data đồng nhất. Chúng ta dùng `Redis`.
- **Cold Storage:** Cơ sở dữ liệu vật lý (ổ cứng). Rất chậm, lưu trữ vĩnh viễn. Dùng `PostgreSQL`.

**Tại sao cần RabbitMQ?** Khi máy tính A update Data X, máy tính A sẽ cập nhật Postgres (Cold) và Redis (Warm). Tuy nhiên máy tính B vẫn lưu phiên bản cũ của X ở trong RAM Caffeine. Do đó Máy A phải bắn tín hiệu lên RabbitMQ kêu gọi *tất cả các máy khác* tự giác xóa (invalidate) cache trong RAM của chúng.

---

## Bước 1. Bổ sung Dependency

Bật file `pom.xml` và thêm **Caffeine** vào block `dependencies`:

```xml
<!-- L1 Cache (Hot Cache) -->
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>
```

---

## Bước 2. Cấu Hình Properties

Bật `src/main/resources/application.properties` để cấu hình các hằng số về Time-To-Live (TTL):

```properties
# ==================== Caching Configuration ====================
# Thời gian lưu Redis (Warm Cache) - ví dụ 60 phút
app.cache.redis.ttl-minutes=60

# Thời gian lưu Caffeine (Hot Cache) - ví dụ 5 phút, 10000 bản ghi
app.cache.caffeine.ttl-minutes=5
app.cache.caffeine.max-size=10000
```

---

## Bước 3. Cấu hình Cache & Redis Beans

### 3.1. Cấu hình Redis
Tạo class `nguyennhatquan.springbootreview.config.RedisConfig` (lưu JSON gọn gàng, thay vì dạng byte mặc định):

```java
package nguyennhatquan.springbootreview.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
```

### 3.2. Cấu hình Caffeine (RAM Cache)
Tạo class `CaffeineConfig` ở package `config`:

```java
package nguyennhatquan.springbootreview.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineConfig {

    @Value("${app.cache.caffeine.ttl-minutes:5}")
    private long ttlMinutes;

    @Value("${app.cache.caffeine.max-size:10000}")
    private long maxSize;

    @Bean
    public Cache<String, Object> caffeineCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
                .maximumSize(maxSize)
                .build();
    }
}
```

---

## Bước 4. Xây dựng kênh liên lạc phân tán RabbitMQ

### 4.1 Tạo Data Format (DTO) Truyền Đi
Tạo `CacheSyncEvent.java` trong thư mục `dto`:
```java
package nguyennhatquan.springbootreview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheSyncEvent implements Serializable {
    private String entityName; // Vd: "product"
    private String action;     // Vd: "update", "delete"
    private String entityId;   // Vd: "1"
}
```

### 4.2 Cấu hình Pub/Sub (Fanout)
Tạo `RabbitMQCacheConfig.java` ở thư mục `config`:
```java
package nguyennhatquan.springbootreview.config;

import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQCacheConfig {
    public static final String CACHE_SYNC_EXCHANGE = "cache.sync.exchange";

    @Bean
    public FanoutExchange cacheSyncExchange() { return new FanoutExchange(CACHE_SYNC_EXCHANGE); }

    // Dùng AnonymousQueue để mỗi Spring Boot khi mở lên sẽ có 1 queue tên khác nhau độc lập.
    @Bean
    public Queue autoDeleteCacheQueue() { return new AnonymousQueue(); }

    @Bean
    public Binding cacheSyncBinding(FanoutExchange exchange, Queue queue) {
        return BindingBuilder.bind(queue).to(exchange);
    }
}
```

### 4.3 Người Phát / Kẻ Trực Tin
Tạo package `service.cache` để gộp 2 file Publisher và Listener:

**CacheMessagePublisher.java:**
```java
package nguyennhatquan.springbootreview.service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nguyennhatquan.springbootreview.config.RabbitMQCacheConfig;
import nguyennhatquan.springbootreview.dto.CacheSyncEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

public class CacheMessagePublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publishCacheInvalidation(String entityName, String entityId, String action) {
        CacheSyncEvent event = CacheSyncEvent.builder()
                .entityName(entityName).entityId(entityId).action(action).build();
        log.info("📢 Gửi RabbitMQ dọn xóa: {} - {}", entityName, entityId);
        // Quăng vào exchange, không cần routing key
        rabbitTemplate.convertAndSend(RabbitMQCacheConfig.CACHE_SYNC_EXCHANGE, "", event);
    }
}
```

**CacheMessageListener.java:**
```java
package nguyennhatquan.springbootreview.service.cache;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nguyennhatquan.springbootreview.dto.CacheSyncEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheMessageListener {
    private final Cache<String, Object> caffeineCache;

    @RabbitListener(queues = "#{autoDeleteCacheQueue.name}")
    public void receiveCacheSyncMessage(CacheSyncEvent event) {
        if (event.getEntityName() != null && event.getEntityId() != null) {
            String key = event.getEntityName().toLowerCase() + "_" + event.getEntityId();
            caffeineCache.invalidate(key); // Cực kỳ cốt lõi: tự xóa dữ liệu rác
            log.info("🧹 Đã dọn RAM bằng lệnh RabbitMQ, key: {}", key);
        }
    }
}
```

---

## Bước 5. Áp Dụng Thực Tế Gắn Trực Tiếp Vào `ProductService`

Bật file `ProductService.java` của bạn và tiêm (`Inject`) 4 thứ bổ sung vào:

```java
import nguyennhatquan.springbootreview.service.cache.CacheMessagePublisher;
import org.springframework.data.redis.core.RedisTemplate;
import com.github.benmanes.caffeine.cache.Cache;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;

// ... (Khai báo class, các repo cũ)
    private final Cache<String, Object> caffeineCache;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheMessagePublisher cacheMessagePublisher;
    private final ObjectMapper objectMapper;

    @Value("${app.cache.redis.ttl-minutes:60}")
    private long redisTtlMinutes;
```

### Cách lấy/truy vấn sản phẩm: Waterfall Đổ Đèo
```java
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        String key = "product_" + id;

        // BƯỚC 1: Tìm dưới RAM (Caffeine)
        Object l1Data = caffeineCache.getIfPresent(key);
        if (l1Data != null) {
            log.info("🎯 [L1-Caffeine] Lấy Product từ RAM nhanh gọn. ID = {}", id);
            return objectMapper.convertValue(l1Data, ProductResponse.class);
        }

        // BƯỚC 2: Tìm dưới Redis
        Object l2Data = redisTemplate.opsForValue().get(key);
        if (l2Data != null) {
            log.info("💧 [L2-Redis] Có ở Redis, sao chép đưa vào RAM.");
            ProductResponse response = objectMapper.convertValue(l2Data, ProductResponse.class);
            caffeineCache.put(key, response);
            return response;
        }

        // BƯỚC 3: Truy vết DB -> Cache lại 2 cái kia
        log.info("❄️ [DB] Tìm sạch vẫn không có, phải query SQL.");
        Product product = productRepository.findById(id).orElseThrow(...);
        ProductResponse response = mapToResponse(product);

        redisTemplate.opsForValue().set(key, response, redisTtlMinutes, java.util.concurrent.TimeUnit.MINUTES);
        caffeineCache.put(key, response);

        return response;
    }
```

### Cách Cập nhật / Xóa: Write-Through + Pub/Sub
Bạn vẫn thao tác lưu vào DB như bình thường, chỉ cần thét thêm lệnh ở cuối hàm:

```java
    // ... Cập nhật xong DB ra được biến 'updated' ...
    ProductResponse response = mapToResponse(updated);
    
    // Ghi đè vào Redis + Caffeine nhà mình
    String key = "product_" + id;
    redisTemplate.opsForValue().set(key, response, redisTtlMinutes, java.util.concurrent.TimeUnit.MINUTES);
    caffeineCache.put(key, response);

    // RA LỆNH CÁC NHÀ KHÁC DỌN DẸP CAFFEINE ĐI
    cacheMessagePublisher.publishCacheInvalidation("product", String.valueOf(id), "update");
```

Việc xóa thì y hệt chỉ đổi từ `caffeineCache.put` thành `caffeineCache.invalidate(key)`, Redis thì `redisTemplate.delete(key)`.

---
*Chúc bạn có quá trình thực hành thật suôn sẻ!*
