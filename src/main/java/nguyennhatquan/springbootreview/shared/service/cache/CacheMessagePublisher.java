package nguyennhatquan.springbootreview.shared.service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nguyennhatquan.springbootreview.shared.config.RabbitMQCacheConfig;
import nguyennhatquan.springbootreview.shared.dto.CacheSyncEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheMessagePublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publishCacheInvalidation(String entityName, String entityId, String action) {
        CacheSyncEvent event = CacheSyncEvent.builder()
                .entityName(entityName)
                .entityId(entityId)
                .action(action)
                .build();
        log.info("📢 Gửi RabbitMQ dọn xóa: {} - {}", entityName, entityId);

        rabbitTemplate.convertAndSend(RabbitMQCacheConfig.CACHE_SYNC_EXCHANGE, "", event);
    }
}
