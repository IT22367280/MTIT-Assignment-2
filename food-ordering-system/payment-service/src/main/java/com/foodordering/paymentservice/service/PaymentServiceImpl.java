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
        Double orderTotalAmount = orderLookupService.getOrderById(paymentRequest.getOrderId()).totalAmount();

        validatePaymentAmount(paymentRequest.getAmount(), orderTotalAmount);
        validateMethodStatusCombination(paymentMethod, paymentStatus);

        Payment payment = Payment.builder()
                .orderId(paymentRequest.getOrderId())
                .amount(paymentRequest.getAmount())
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .build();

        return toResponse(paymentRepository.save(payment));
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
        String paymentStatus = normalizePaymentStatus(paymentStatusUpdateRequest.getPaymentStatus());

        validateMethodStatusCombination(normalizePaymentMethod(payment.getPaymentMethod()), paymentStatus);
        payment.setPaymentStatus(paymentStatus);

        return toResponse(paymentRepository.save(payment));
    }

    @Override
    public void deletePayment(Long id) {
        Payment payment = findPaymentById(id);
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

    private String normalizePaymentMethod(String paymentMethod) {
        return paymentMethod.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizePaymentStatus(String paymentStatus) {
        return paymentStatus.trim()
                .replace(' ', '_')
                .toUpperCase(Locale.ROOT);
    }
}
