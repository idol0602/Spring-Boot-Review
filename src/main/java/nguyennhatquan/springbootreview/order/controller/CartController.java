package nguyennhatquan.springbootreview.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nguyennhatquan.springbootreview.order.dto.cart.AddToCartRequest;
import nguyennhatquan.springbootreview.order.dto.cart.CartResponse;
import nguyennhatquan.springbootreview.order.dto.cart.UpdateCartItemRequest;
import nguyennhatquan.springbootreview.shared.dto.ApiResponse;
import nguyennhatquan.springbootreview.shared.security.CustomUserDetails;
import nguyennhatquan.springbootreview.order.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Get cart for user: {}", userDetails.getUsername());

        CartResponse data = cartService.getCart(userDetails.getId());

        ApiResponse<CartResponse> response = ApiResponse.<CartResponse>builder()
                .code(200)
                .message("Cart retrieved successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/items")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @Valid @RequestBody AddToCartRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Add item {} with quantity {} to cart for user {}", request.getProductId(), request.getQuantity(), userDetails.getUsername());

        CartResponse data = cartService.addItem(userDetails.getId(), request.getProductId(), request.getQuantity());

        ApiResponse<CartResponse> response = ApiResponse.<CartResponse>builder()
                .code(200)
                .message("Item added to cart successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/items/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Update item {} with quantity {} for user {}", productId, request.getQuantity(), userDetails.getUsername());

        CartResponse data = cartService.updateItem(userDetails.getId(), productId, request.getQuantity());

        ApiResponse<CartResponse> response = ApiResponse.<CartResponse>builder()
                .code(200)
                .message("Item updated successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Remove item {} from cart", productId);

        CartResponse data = cartService.removeItem(userDetails.getId(), productId);

        ApiResponse<CartResponse> response = ApiResponse.<CartResponse>builder()
                .code(200)
                .message("Item removed successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Object>> clearCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Clear cart for user: {}", userDetails.getUsername());

        cartService.clearCart(userDetails.getId());

        ApiResponse<Object> response = ApiResponse.builder()
                .code(200)
                .message("Cart cleared successfully")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(response);
    }
}
