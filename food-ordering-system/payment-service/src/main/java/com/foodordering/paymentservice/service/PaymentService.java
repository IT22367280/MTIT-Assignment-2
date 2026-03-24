package com.foodordering.paymentservice.service;

import com.foodordering.paymentservice.dto.PaymentRequest;
import com.foodordering.paymentservice.dto.PaymentResponse;
import com.foodordering.paymentservice.dto.PaymentStatusUpdateRequest;
import java.util.List;

public interface PaymentService {

    PaymentResponse createPayment(PaymentRequest paymentRequest);

    List<PaymentResponse> getAllPayments();

    PaymentResponse getPaymentById(Long id);

    PaymentResponse updatePaymentStatus(Long id, PaymentStatusUpdateRequest paymentStatusUpdateRequest);

    void deletePayment(Long id);
}
