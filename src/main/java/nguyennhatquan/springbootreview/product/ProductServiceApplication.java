package nguyennhatquan.springbootreview.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(excludeName = {
    "org.springframework.cloud.gateway.config.GatewayAutoConfiguration",
    "org.springframework.cloud.gateway.config.GatewayClassPathWarningAutoConfiguration",
    "org.springframework.cloud.gateway.config.GatewayRedisAutoConfiguration"
})
@ComponentScan(basePackages = {"nguyennhatquan.springbootreview.product", "nguyennhatquan.springbootreview.shared"})
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
