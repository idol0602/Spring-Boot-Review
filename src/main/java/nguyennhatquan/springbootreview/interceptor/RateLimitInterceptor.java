package nguyennhatquan.springbootreview.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {
    private final RedisTemplate<String, String> redisTemplate;
    private static final int MAX_REQUESTS = 100; // Maximum requests per minute
    private static final long WINDOW_SIZE = 60; // 60 seconds = 1 minute

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIP = getClientIP(request);
        String key = "rate_limit:" + clientIP;

        try {
            String count = redisTemplate.opsForValue().get(key);
            int requests = count != null ? Integer.parseInt(count) : 0;

            if (requests >= MAX_REQUESTS) {
                log.warn("Rate limit exceeded for IP: {}", clientIP);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":429,\"message\":\"Too many requests\",\"timestamp\":\"" + java.time.LocalDateTime.now() + "\"}");
                return false;
            }

            redisTemplate.opsForValue().increment(key);
            if(count == null) {
                redisTemplate.expire(key, WINDOW_SIZE, TimeUnit.SECONDS);
            }


        } catch (Exception e) {
            log.error("Rate limit check failed", e);
        }

        return true;
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if(xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

}
