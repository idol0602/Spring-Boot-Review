package nguyennhatquan.springbootreview.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nguyennhatquan.springbootreview.dto.*;
import nguyennhatquan.springbootreview.entity.User;
import nguyennhatquan.springbootreview.repository.UserRepository;
import nguyennhatquan.springbootreview.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getUserOrders(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            Authentication authentication) {
        log.info("Get orders for user: {}", authentication.getName());
        User user = userRepository.findByEmail(authentication.getName()).get();
        PageResponse<OrderResponse> data = orderService.getUserOrders(user.getId(), pageNo, pageSize);

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
            Authentication authentication) {
        log.info("Get order by id: {}", id);

        User user = userRepository.findByEmail(authentication.getName()).get();
        OrderResponse data = orderService.getById(id, user.getId());

        ApiResponse<OrderResponse> response = ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Order retrieved successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        log.info("Create order for user: {}", authentication.getName());

        User user = userRepository.findByEmail(authentication.getName()).get();
        OrderResponse data = orderService.createOrderFromCart(user.getId(), request.getShippingAddress());

        ApiResponse<OrderResponse> response = ApiResponse.<OrderResponse>builder()
                .code(201)
                .message("Order created successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
