package nguyennhatquan.springbootreview.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import nguyennhatquan.springbootreview.config.MomoConfig;
import nguyennhatquan.springbootreview.dto.momo.MomoCallbackRequest;
import nguyennhatquan.springbootreview.dto.momo.MomoCreatePaymentRequest;
import nguyennhatquan.springbootreview.dto.momo.MomoCreatePaymentResponse;
import nguyennhatquan.springbootreview.entity.Order;
import nguyennhatquan.springbootreview.entity.Payment;
import nguyennhatquan.springbootreview.entity.PaymentStatus;
import nguyennhatquan.springbootreview.repository.OrderRepository;
import nguyennhatquan.springbootreview.repository.PaymentRepository;
import nguyennhatquan.springbootreview.util.MomoSignatureUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MomoPaymentService {
    private final MomoConfig momoConfig;
    private final MomoSignatureUtil signatureUtil;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;
    private final MomoSignatureUtil momoSignatureUtil;

    @Transactional
    public MomoCreatePaymentResponse createPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));

        String momoOrderId = "ORDER_" + orderId + "_" + System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();
        Long amount = order.getTotalAmount().longValue();
        String orderInfo = "Pay for order " + orderId;
        String extraData = "";

        Map<String,String> signParams = new TreeMap<>();
        signParams.put("accessKey", momoConfig.getAccessKey());
        signParams.put("amount", String.valueOf(amount));
        signParams.put("extraData", extraData);
        signParams.put("ipnUrl", momoConfig.getIpnUrl());
        signParams.put("orderId", momoOrderId);
        signParams.put("orderInfo", orderInfo);
        signParams.put("partnerCode", momoConfig.getPartnerCode());
        signParams.put("redirectUrl", momoConfig.getRedirectURL());
        signParams.put("requestId", requestId);
        signParams.put("requestType", momoConfig.getRequestType());

        String rawSignature = signatureUtil.buildRawSignature(signParams);
        String signature = signatureUtil.sign(rawSignature, momoConfig.getSecretKey());

        MomoCreatePaymentRequest request = MomoCreatePaymentRequest.builder()
                .partnerCode(momoConfig.getPartnerCode())
                .accessKey(momoConfig.getAccessKey())
                .requestId(requestId)
                .amount(amount)
                .orderId(momoOrderId)
                .orderInfo(orderInfo)
                .redirectUrl(momoConfig.getRedirectURL())
                .ipnUrl(momoConfig.getIpnUrl())
                .extraData(extraData)
                .requestType(momoConfig.getRequestType())
                .signature(signature)
                .lang("en")
                .build();

        MomoCreatePaymentResponse response = restTemplate.postForObject(momoConfig.getEndpoint(), request, MomoCreatePaymentResponse.class);

        if (response != null) {
            Payment payment = Payment.builder()
                    .orderId(orderId)
                    .momoOrderId(momoOrderId)
                    .requestId(requestId)
                    .amount(BigDecimal.valueOf(amount))
                    .status(PaymentStatus.PENDING)
                    .payUrl(response.getPayUrl())
                    .build();
            paymentRepository.save(payment);
        }

        return response;
    }

    @Transactional
    public void handleCallBack(MomoCallbackRequest callback) {
        Map<String, String> signParams = new TreeMap<>();
        signParams.put("accessKey", momoConfig.getAccessKey());
        signParams.put("amount", callback.getAmount() != null ? String.valueOf(callback.getAmount()) : "");
        signParams.put("extraData", callback.getExtraData() != null ? callback.getExtraData() : "");
        signParams.put("message", callback.getMessage() != null ? callback.getMessage() : "");
        signParams.put("orderId", callback.getOrderId() != null ? callback.getOrderId() : "");
        signParams.put("orderInfo", callback.getOrderInfo() != null ? callback.getOrderInfo() : "");
        signParams.put("orderType", callback.getOrderType() != null ? callback.getOrderType() : "");
        signParams.put("partnerCode", callback.getPartnerCode() != null ? callback.getPartnerCode() : "");
        signParams.put("payType", callback.getPayType() != null ? callback.getPayType() : "");
        signParams.put("requestId", callback.getRequestId() != null ? callback.getRequestId() : "");
        signParams.put("responseTime", callback.getResponseTime() != null ? String.valueOf(callback.getResponseTime()) : "");
        signParams.put("resultCode", callback.getResultCode() != null ? String.valueOf(callback.getResultCode()) : "");
        signParams.put("transId", callback.getTransId() != null ? String.valueOf(callback.getTransId()) : "");

        if(!momoSignatureUtil.verify(signParams, callback.getSignature(), momoConfig.getSecretKey())) {
            throw new SecurityException("Invalid Momo signature");
        }

        Payment payment = paymentRepository.findByMomoOrderId(callback.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for MoMo Order ID: " + callback.getOrderId()));

        boolean isSuccess = callback.getResultCode() == 0;

        payment.setStatus(isSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        payment.setTransactionId(callback.getTransId());
        payment.setResultCode(callback.getResultCode());
        payment.setMessage(callback.getMessage());

        paymentRepository.save(payment);
    }
}
