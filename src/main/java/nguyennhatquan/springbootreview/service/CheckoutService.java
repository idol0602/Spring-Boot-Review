package nguyennhatquan.springbootreview.service;

import lombok.RequiredArgsConstructor;
import nguyennhatquan.springbootreview.dto.momo.MomoCallbackRequest;
import nguyennhatquan.springbootreview.dto.order.CheckoutRequest;
import nguyennhatquan.springbootreview.entity.Order;
import nguyennhatquan.springbootreview.entity.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final OrderService orderService;
    private final MomoPaymentService momoPaymentService;

    @Transactional
    public String createOrderAndPaymentForMomo(CheckoutRequest request, Long userId) {
        Order order = orderService.createOrder(request, userId);
        return momoPaymentService.createPayment(order.getId()).getPayUrl();
    }

    @Transactional
    public void handleMomoCallback(MomoCallbackRequest callbackRequest) {
        momoPaymentService.handleCallBack(callbackRequest);
        Long orderId = extractOrderId(callbackRequest.getOrderId());

        if (callbackRequest.getResultCode() == 0) {
            orderService.updateStatus(orderId, OrderStatus.CONFIRMED);
        } else {
            orderService.restoreOrderItems(orderId);
        }
    }

    private Long extractOrderId(String momoOrderId) {
        String[] parts = momoOrderId.split("_");
        return Long.parseLong(parts[1]);
    }
}
