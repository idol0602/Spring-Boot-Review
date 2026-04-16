package nguyennhatquan.springbootreview.order.service;

import lombok.RequiredArgsConstructor;
import nguyennhatquan.springbootreview.order.dto.momo.MomoCallbackRequest;
import nguyennhatquan.springbootreview.order.dto.momo.MomoCreatePaymentResponse;
import nguyennhatquan.springbootreview.order.dto.order.CheckoutRequest;
import nguyennhatquan.springbootreview.shared.entity.Order;
import nguyennhatquan.springbootreview.shared.entity.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final OrderService orderService;
    private final MomoPaymentService momoPaymentService;

    @Transactional
    public MomoCreatePaymentResponse createOrderAndPaymentForMomo(CheckoutRequest request, Long userId) {
        Order order = orderService.createOrder(request, userId);
        return momoPaymentService.createPayment(order.getId());
    }

    @Transactional
    public void handleMomoCallback(MomoCallbackRequest callbackRequest) {
        momoPaymentService.handleCallBack(callbackRequest);
        Long orderId = extractOrderId(callbackRequest.getOrderId());

        if (callbackRequest.getResultCode() == 0) {
            orderService.updateStatus(orderId, OrderStatus.CONFIRMED);
        } else {
            orderService.updateStatus(orderId, OrderStatus.CANCELLED);
            orderService.restoreOrderItems(orderId);
        }
    }

    private Long extractOrderId(String momoOrderId) {
        String[] parts = momoOrderId.split("_");
        return Long.parseLong(parts[1]);
    }
}
