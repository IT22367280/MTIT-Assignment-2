package com.foodordering.paymentservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.foodordering.paymentservice.dto.OrderSummaryResponse;
import com.foodordering.paymentservice.dto.PaymentRequest;
import com.foodordering.paymentservice.dto.PaymentStatusUpdateRequest;
import com.foodordering.paymentservice.model.Payment;
import com.foodordering.paymentservice.repository.PaymentRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderLookupService orderLookupService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void createPaymentSynchronizesOrderToCompleteWhenPaymentIsPaid() {
        PaymentRequest request = new PaymentRequest(2L, 2200.00, "CARD", "PAID");
        Payment savedPayment = Payment.builder()
                .id(9L)
                .orderId(2L)
                .amount(2200.00)
                .paymentMethod("CARD")
                .paymentStatus("PAID")
                .build();

        when(orderLookupService.getOrderById(2L))
                .thenReturn(new OrderSummaryResponse(2L, 2200.00, null, "PENDING"));
        when(paymentRepository.existsByOrderId(2L)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        var response = paymentService.createPayment(request);

        assertThat(response.paymentStatus()).isEqualTo("PAID");
        verify(orderLookupService).linkPaymentToOrder(2L, 9L);
        verify(orderLookupService).updateOrderStatus(2L, "COMPLETE");
    }

    @Test
    void createPaymentSynchronizesOrderToReceivedWhenPaymentIsOnDelivery() {
        PaymentRequest request = new PaymentRequest(2L, 2200.00, "CASH", "ON DELIVERY");
        Payment savedPayment = Payment.builder()
                .id(9L)
                .orderId(2L)
                .amount(2200.00)
                .paymentMethod("CASH")
                .paymentStatus("ON_DELIVERY")
                .build();

        when(orderLookupService.getOrderById(2L))
                .thenReturn(new OrderSummaryResponse(2L, 2200.00, null, "PENDING"));
        when(paymentRepository.existsByOrderId(2L)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        var response = paymentService.createPayment(request);

        assertThat(response.paymentStatus()).isEqualTo("ON_DELIVERY");
        verify(orderLookupService).linkPaymentToOrder(2L, 9L);
        verify(orderLookupService).updateOrderStatus(2L, "RECEIVED");
    }

    @Test
    void updatePaymentStatusSynchronizesOrderToCompleteWhenPaymentBecomesPaid() {
        Payment payment = Payment.builder()
                .id(9L)
                .orderId(2L)
                .amount(2200.00)
                .paymentMethod("CASH")
                .paymentStatus("ON_DELIVERY")
                .build();

        when(paymentRepository.findById(9L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderLookupService.getOrderById(2L))
                .thenReturn(new OrderSummaryResponse(2L, 2200.00, 9L, "RECEIVED"));

        var response = paymentService.updatePaymentStatus(9L, new PaymentStatusUpdateRequest("paid"));

        assertThat(response.paymentStatus()).isEqualTo("PAID");
        verify(orderLookupService).updateOrderStatus(2L, "COMPLETE");
    }
}
