package com.foodordering.orderservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.foodordering.orderservice.dto.CustomerSummaryResponse;
import com.foodordering.orderservice.dto.FoodOrderRequest;
import com.foodordering.orderservice.dto.OrderStatusUpdateRequest;
import com.foodordering.orderservice.dto.PaymentSummaryResponse;
import com.foodordering.orderservice.model.FoodOrder;
import com.foodordering.orderservice.repository.FoodOrderRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FoodOrderServiceImplTest {

    @Mock
    private FoodOrderRepository foodOrderRepository;

    @Mock
    private CustomerLookupService customerLookupService;

    @Mock
    private PaymentLookupService paymentLookupService;

    @InjectMocks
    private FoodOrderServiceImpl foodOrderService;

    @Test
    void createOrderRejectsCompleteStatusBeforePaymentExists() {
        FoodOrderRequest request = new FoodOrderRequest(2L, "Cheese Pizza", 1, 2200.00, "COMPLETE");

        when(customerLookupService.getCustomerById(2L))
                .thenReturn(new CustomerSummaryResponse(2L, "Brian Lee"));

        assertThatThrownBy(() -> foodOrderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order cannot be created as COMPLETE before payment is PAID");
    }

    @Test
    void updateOrderStatusAllowsCompleteWhenLinkedPaymentIsPaid() {
        FoodOrder foodOrder = FoodOrder.builder()
                .id(1L)
                .customerId(2L)
                .itemName("Cheese Pizza")
                .quantity(1)
                .totalAmount(2200.00)
                .paymentId(9L)
                .status("RECEIVED")
                .build();

        when(foodOrderRepository.findById(1L)).thenReturn(Optional.of(foodOrder));
        when(foodOrderRepository.save(any(FoodOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(customerLookupService.getCustomerById(2L))
                .thenReturn(new CustomerSummaryResponse(2L, "Brian Lee"));
        when(paymentLookupService.getPaymentById(9L))
                .thenReturn(new PaymentSummaryResponse(9L, "PAID"));

        var response = foodOrderService.updateOrderStatus(1L, new OrderStatusUpdateRequest("complete"));

        assertThat(response.status()).isEqualTo("COMPLETE");
        verify(foodOrderRepository).save(foodOrder);
    }

    @Test
    void updateOrderStatusRejectsCompleteWhenLinkedPaymentIsNotPaid() {
        FoodOrder foodOrder = FoodOrder.builder()
                .id(1L)
                .customerId(2L)
                .itemName("Cheese Pizza")
                .quantity(1)
                .totalAmount(2200.00)
                .paymentId(9L)
                .status("RECEIVED")
                .build();

        when(foodOrderRepository.findById(1L)).thenReturn(Optional.of(foodOrder));
        when(paymentLookupService.getPaymentById(9L))
                .thenReturn(new PaymentSummaryResponse(9L, "ON_DELIVERY"));

        assertThatThrownBy(() -> foodOrderService.updateOrderStatus(1L, new OrderStatusUpdateRequest("COMPLETE")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order can be marked COMPLETE only when payment status is PAID");
    }

    @Test
    void updateOrderStatusRejectsDowngradeWhenLinkedPaymentIsPaid() {
        FoodOrder foodOrder = FoodOrder.builder()
                .id(1L)
                .customerId(2L)
                .itemName("Cheese Pizza")
                .quantity(1)
                .totalAmount(2200.00)
                .paymentId(9L)
                .status("COMPLETE")
                .build();

        when(foodOrderRepository.findById(1L)).thenReturn(Optional.of(foodOrder));
        when(paymentLookupService.getPaymentById(9L))
                .thenReturn(new PaymentSummaryResponse(9L, "PAID"));

        assertThatThrownBy(() -> foodOrderService.updateOrderStatus(1L, new OrderStatusUpdateRequest("RECEIVED")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Orders with PAID payments must remain COMPLETE unless cancelled");
    }

    @Test
    void getAllOrdersFallsBackToCustomerIdentifierWhenCustomerLookupFails() {
        FoodOrder foodOrder = FoodOrder.builder()
                .id(4L)
                .customerId(99L)
                .itemName("Burger")
                .quantity(2)
                .totalAmount(1800.00)
                .status("PENDING")
                .build();

        when(foodOrderRepository.findAll()).thenReturn(List.of(foodOrder));
        when(customerLookupService.getCustomerById(99L))
                .thenThrow(new IllegalStateException("Customer service is unavailable"));

        var response = foodOrderService.getAllOrders();

        assertThat(response).singleElement()
                .extracting(order -> order.customerName())
                .isEqualTo("Customer #99");
    }
}
