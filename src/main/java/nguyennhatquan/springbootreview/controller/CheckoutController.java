package nguyennhatquan.springbootreview.controller;

import lombok.RequiredArgsConstructor;
import nguyennhatquan.springbootreview.dto.momo.MomoCallbackRequest;
import nguyennhatquan.springbootreview.dto.order.CheckoutRequest;
import nguyennhatquan.springbootreview.security.CustomUserDetails;
import nguyennhatquan.springbootreview.service.CheckoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping("/momo")
    public ResponseEntity<String> createOrderAndPaymentForMomo(
            @RequestBody CheckoutRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String payUrl = checkoutService.createOrderAndPaymentForMomo(request, userDetails.getId());
        return ResponseEntity.ok(payUrl);
    }

    @PostMapping("/momo/callback")
    public ResponseEntity<Void> handleMomoCallback(@RequestBody MomoCallbackRequest callbackRequest) {
        checkoutService.handleMomoCallback(callbackRequest);
        return ResponseEntity.noContent().build();
    }
}

