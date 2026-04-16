package nguyennhatquan.springbootreview.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(excludeName = {
    "org.springframework.cloud.gateway.config.GatewayAutoConfiguration",
    "org.springframework.cloud.gateway.config.GatewayClassPathWarningAutoConfiguration",
    "org.springframework.cloud.gateway.config.GatewayRedisAutoConfiguration"
})
@ComponentScan(basePackages = {
    "nguyennhatquan.springbootreview.payment",
    "nguyennhatquan.springbootreview.order",
    "nguyennhatquan.springbootreview.shared"
})
@EntityScan(basePackages = "nguyennhatquan.springbootreview")
@EnableJpaRepositories(basePackages = "nguyennhatquan.springbootreview")
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
