package nguyennhatquan.springbootreview.shared.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQCacheConfig {
    public static final String CACHE_SYNC_EXCHANGE = "cache.sync.exchange";

    // ── Message converter: dùng JSON thay vì Java serialization ──────────────
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    // ── RabbitTemplate dùng JSON converter để publish ─────────────────────────
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    // ── Listener container factory dùng JSON converter để consume ─────────────
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        return factory;
    }

    // ── Queue / Exchange / Binding ────────────────────────────────────────────
    @Bean
    public FanoutExchange cacheSyncExchange() { return new FanoutExchange(CACHE_SYNC_EXCHANGE); }

    // Dùng AnonymousQueue để mỗi Spring Boot khi mở lên sẽ có 1 queue tên khác nhau độc lập.
    @Bean
    public Queue autoDeleteCacheQueue() { return new AnonymousQueue(); }

    @Bean
    public Binding cacheSyncBinding(FanoutExchange exchange, Queue autoDeleteCacheQueue) {
        return BindingBuilder.bind(autoDeleteCacheQueue).to(exchange);
    }
}