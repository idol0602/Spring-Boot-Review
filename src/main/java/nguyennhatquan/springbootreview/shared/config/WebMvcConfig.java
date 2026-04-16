package nguyennhatquan.springbootreview.shared.config;

import lombok.RequiredArgsConstructor;
import nguyennhatquan.springbootreview.shared.interceptor.LoggingInterceptor;
import nguyennhatquan.springbootreview.shared.interceptor.RateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final LoggingInterceptor loggingInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Rate limit interceptor first (most restrictive)
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**")
                .order(1);

        // Logging interceptor last
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/api/**")
                .order(2);
    }
}


