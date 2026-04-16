package nguyennhatquan.springbootreview.shared.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "nguyennhatquan.springbootreview.shared.entity")
@EnableJpaRepositories(basePackages = "nguyennhatquan.springbootreview.shared.repository")
public class DatabaseConfig {
}
