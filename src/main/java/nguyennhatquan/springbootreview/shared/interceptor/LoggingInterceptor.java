package nguyennhatquan.springbootreview.shared.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);

        log.info("==> REQUEST [{}] {}", request.getMethod(), request.getRequestURI());
        log.debug("Headers: {}", getHeadersInfo(request));

        return true; // Cho phép tiếp tục xử lý request
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long startTime = (long) request.getAttribute("startTime");
        long duration = System.currentTimeMillis() - startTime;

        log.info("<== RESPONSE [{}] {} - {} ms", request.getMethod(), request.getRequestURI(), duration);
        log.debug("Status: {}", response.getStatus());

        if (ex != null) {
            log.error("Error occurred", ex);
        }
    }

    private String getHeadersInfo(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        request.getHeaderNames().asIterator().forEachRemaining(name -> sb.append(name).append("=").append(request.getHeader(name)).append("; "));
        return sb.toString();
    }
}

