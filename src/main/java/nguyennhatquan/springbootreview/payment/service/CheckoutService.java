package nguyennhatquan.springbootreview.payment.service;

import lombok.RequiredArgsConstructor;
import nguyennhatquan.springbootreview.order.service.OrderService;
import nguyennhatquan.springbootreview.payment.dto.momo.MomoCallbackRequest;
import nguyennhatquan.springbootreview.payment.dto.momo.MomoCreatePaymentResponse;
import nguyennhatquan.springbootreview.order.dto.order.CheckoutRequest;
import nguyennhatquan.springbootreview.order.entity.Order;
import nguyennhatquan.springbootreview.order.entity.OrderStatus;
import nguyennhatquan.springbootreview.payment.service.momo.MomoPaymentService;
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
