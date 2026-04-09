package nguyennhatquan.springbootreview.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nguyennhatquan.springbootreview.dto.common.ApiResponse;
import nguyennhatquan.springbootreview.dto.common.PageResponse;
import nguyennhatquan.springbootreview.dto.order.OrderResponse;
import nguyennhatquan.springbootreview.dto.order.UpdateOrderRequest;
import nguyennhatquan.springbootreview.security.CustomUserDetails;
import nguyennhatquan.springbootreview.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getUserOrders(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Get orders for user: {}", userDetails.getUsername());
        PageResponse<OrderResponse> data = orderService.getUserOrders(userDetails.getId(), pageNo, pageSize);

        ApiResponse<PageResponse<OrderResponse>> response = ApiResponse.<PageResponse<OrderResponse>>builder()
                .code(200)
                .message("Orders retrieved successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderResponse>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Get order by id: {}", id);

        OrderResponse data = orderService.getById(id, userDetails.getId());

        ApiResponse<OrderResponse> response = ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Order retrieved successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderRequest request) {
        log.info("Update order {} status to {}", id, request.getStatus());

        OrderResponse data = orderService.updateStatus(id, request.getStatus());

        ApiResponse<OrderResponse> response = ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Order status updated successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long id) {
        log.info("Delete order with id: {}", id);

        orderService.deleteOrder(id);

        ApiResponse<Object> response = ApiResponse.builder()
                .code(200)
                .message("Order deleted successfully")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }
}
