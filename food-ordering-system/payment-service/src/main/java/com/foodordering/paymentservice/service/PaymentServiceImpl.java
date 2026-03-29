package com.foodordering.paymentservice.service;

import com.foodordering.paymentservice.dto.PaymentRequest;
import com.foodordering.paymentservice.dto.PaymentResponse;
import com.foodordering.paymentservice.dto.PaymentStatusUpdateRequest;
import com.foodordering.paymentservice.exception.ResourceNotFoundException;
import com.foodordering.paymentservice.model.Payment;
import com.foodordering.paymentservice.repository.PaymentRepository;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderLookupService orderLookupService;

    @Override
    public PaymentResponse createPayment(PaymentRequest paymentRequest) {
        String paymentMethod = normalizePaymentMethod(paymentRequest.getPaymentMethod());
        String paymentStatus = normalizePaymentStatus(paymentRequest.getPaymentStatus());
        Long orderId = paymentRequest.getOrderId();
        var order = orderLookupService.getOrderById(orderId);
        Double orderTotalAmount = order.totalAmount();

        validateOrderIsActive(order.status());
        validatePaymentAmount(paymentRequest.getAmount(), orderTotalAmount);
        validateMethodStatusCombination(paymentMethod, paymentStatus);
        validateOrderPaymentLink(orderId, order.paymentId());

        Payment payment = Payment.builder()
                .orderId(orderId)
                .amount(paymentRequest.getAmount())
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        try {
            orderLookupService.linkPaymentToOrder(orderId, savedPayment.getId());
            synchronizeOrderStatus(orderId, paymentStatus);
        } catch (RuntimeException exception) {
            safelyRollbackOrderLink(orderId);
            paymentRepository.delete(savedPayment);
            throw new IllegalStateException("Payment was created but could not be synchronized with the order", exception);
        }

        return toResponse(savedPayment);
    }

    @Override
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public PaymentResponse getPaymentById(Long id) {
        return toResponse(findPaymentById(id));
    }

    @Override
    public PaymentResponse updatePaymentStatus(Long id, PaymentStatusUpdateRequest paymentStatusUpdateRequest) {
        Payment payment = findPaymentById(id);
        String previousPaymentStatus = payment.getPaymentStatus();
        String paymentStatus = normalizePaymentStatus(paymentStatusUpdateRequest.getPaymentStatus());
        var order = orderLookupService.getOrderById(payment.getOrderId());

        validateOrderIsActive(order.status());
        validateMethodStatusCombination(normalizePaymentMethod(payment.getPaymentMethod()), paymentStatus);
        payment.setPaymentStatus(paymentStatus);
        Payment savedPayment = paymentRepository.save(payment);

        try {
            synchronizeOrderStatus(savedPayment.getOrderId(), paymentStatus);
        } catch (RuntimeException exception) {
            savedPayment.setPaymentStatus(previousPaymentStatus);
            paymentRepository.save(savedPayment);
            throw new IllegalStateException("Payment status was updated but could not be synchronized with the order", exception);
        }

        return toResponse(savedPayment);
    }

    @Override
    public void deletePayment(Long id) {
        Payment payment = findPaymentById(id);
        var order = orderLookupService.getOrderById(payment.getOrderId());
        if (id.equals(order.paymentId())) {
            orderLookupService.unlinkPaymentFromOrder(payment.getOrderId());
            if (!"CANCELLED".equalsIgnoreCase(order.status())) {
                orderLookupService.updateOrderStatus(payment.getOrderId(), "PENDING");
            }
        }
        paymentRepository.delete(payment);
    }

    private Payment findPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id " + id));
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .build();
    }

    private void validatePaymentAmount(Double paymentAmount, Double orderTotalAmount) {
        if (Double.compare(paymentAmount, orderTotalAmount) != 0) {
            throw new IllegalArgumentException("Payment amount must match order total amount " + orderTotalAmount);
        }
    }

    private void validateMethodStatusCombination(String paymentMethod, String paymentStatus) {
        if ("CARD".equals(paymentMethod) && !"PAID".equals(paymentStatus)) {
            throw new IllegalArgumentException("CARD payments must use PAID status");
        }
    }

    private void validateOrderPaymentLink(Long orderId, Long paymentId) {
        if (paymentId != null || paymentRepository.existsByOrderId(orderId)) {
            throw new IllegalArgumentException("Order already has a linked payment");
        }
    }

    private void validateOrderIsActive(String orderStatus) {
        if (orderStatus != null && "CANCELLED".equalsIgnoreCase(orderStatus)) {
            throw new IllegalArgumentException("Cancelled orders cannot accept payment changes");
        }
    }

    private String normalizePaymentMethod(String paymentMethod) {
        return paymentMethod.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizePaymentStatus(String paymentStatus) {
        return paymentStatus.trim()
                .replace(' ', '_')
                .toUpperCase(Locale.ROOT);
    }

    private void synchronizeOrderStatus(Long orderId, String paymentStatus) {
        orderLookupService.updateOrderStatus(orderId, mapOrderStatusFromPayment(paymentStatus));
    }

    private String mapOrderStatusFromPayment(String paymentStatus) {
        return "PAID".equals(paymentStatus) ? "COMPLETE" : "RECEIVED";
    }

    private void safelyRollbackOrderLink(Long orderId) {
        try {
            orderLookupService.unlinkPaymentFromOrder(orderId);
        } catch (RuntimeException ignored) {
            // Best effort rollback to avoid leaving an order linked to a deleted payment.
        }
    }
}
