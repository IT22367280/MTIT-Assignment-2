package com.foodordering.orderservice.service;

import com.foodordering.orderservice.dto.FoodOrderRequest;
import com.foodordering.orderservice.dto.FoodOrderResponse;
import com.foodordering.orderservice.dto.OrderPaymentUpdateRequest;
import com.foodordering.orderservice.dto.OrderStatusUpdateRequest;
import com.foodordering.orderservice.exception.ResourceNotFoundException;
import com.foodordering.orderservice.model.FoodOrder;
import com.foodordering.orderservice.repository.FoodOrderRepository;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FoodOrderServiceImpl implements FoodOrderService {

    private final FoodOrderRepository foodOrderRepository;
    private final CustomerLookupService customerLookupService;
    private final PaymentLookupService paymentLookupService;

    @Override
    public FoodOrderResponse createOrder(FoodOrderRequest foodOrderRequest) {
        customerLookupService.getCustomerById(foodOrderRequest.getCustomerId());
        String normalizedStatus = normalizeStatus(foodOrderRequest.getStatus());
        validateStatusForNewOrder(normalizedStatus);

        FoodOrder foodOrder = FoodOrder.builder()
                .customerId(foodOrderRequest.getCustomerId())
                .itemName(foodOrderRequest.getItemName())
                .quantity(foodOrderRequest.getQuantity())
                .totalAmount(foodOrderRequest.getTotalAmount())
                .status(normalizedStatus)
                .build();

        return toResponse(foodOrderRepository.save(foodOrder));
    }

    @Override
    public List<FoodOrderResponse> getAllOrders() {
        return foodOrderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public FoodOrderResponse getOrderById(Long id) {
        return toResponse(findOrderById(id));
    }

    @Override
    public FoodOrderResponse updateOrderPayment(Long id, OrderPaymentUpdateRequest orderPaymentUpdateRequest) {
        FoodOrder foodOrder = findOrderById(id);
        foodOrder.setPaymentId(orderPaymentUpdateRequest.getPaymentId());

        return toResponse(foodOrderRepository.save(foodOrder));
    }

    @Override
    public FoodOrderResponse updateOrderStatus(Long id, OrderStatusUpdateRequest orderStatusUpdateRequest) {
        FoodOrder foodOrder = findOrderById(id);
        String normalizedStatus = normalizeStatus(orderStatusUpdateRequest.getStatus());
        validateStatusAgainstLinkedPayment(foodOrder, normalizedStatus);
        foodOrder.setStatus(normalizedStatus);

        return toResponse(foodOrderRepository.save(foodOrder));
    }

    @Override
    public void deleteOrder(Long id) {
        FoodOrder foodOrder = findOrderById(id);
        foodOrderRepository.delete(foodOrder);
    }

    private FoodOrder findOrderById(Long id) {
        return foodOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id));
    }

    private FoodOrderResponse toResponse(FoodOrder foodOrder) {
        String customerName = resolveCustomerName(foodOrder.getCustomerId());

        return FoodOrderResponse.builder()
                .id(foodOrder.getId())
                .customerId(foodOrder.getCustomerId())
                .customerName(customerName)
                .itemName(foodOrder.getItemName())
                .quantity(foodOrder.getQuantity())
                .totalAmount(foodOrder.getTotalAmount())
                .paymentId(foodOrder.getPaymentId())
                .status(foodOrder.getStatus())
                .build();
    }

    private String resolveCustomerName(Long customerId) {
        try {
            return customerLookupService.getCustomerById(customerId).fullName();
        } catch (RuntimeException exception) {
            return "Customer #" + customerId;
        }
    }

    private String normalizeStatus(String status) {
        return status.trim().toUpperCase(Locale.ROOT);
    }

    private void validateStatusForNewOrder(String normalizedStatus) {
        if ("COMPLETE".equals(normalizedStatus)) {
            throw new IllegalArgumentException("Order cannot be created as COMPLETE before payment is PAID");
        }
    }

    private void validateStatusAgainstLinkedPayment(FoodOrder foodOrder, String targetStatus) {
        if ("CANCELLED".equals(targetStatus)) {
            return;
        }

        Long paymentId = foodOrder.getPaymentId();
        if (paymentId == null) {
            if ("COMPLETE".equals(targetStatus)) {
                throw new IllegalArgumentException("Order cannot be marked COMPLETE without a linked payment");
            }
            return;
        }

        String paymentStatus = paymentLookupService.getPaymentById(paymentId).paymentStatus();
        if (paymentStatus == null) {
            throw new IllegalStateException("Payment service returned a payment without a status");
        }

        String normalizedPaymentStatus = paymentStatus.trim().toUpperCase(Locale.ROOT);
        if ("PAID".equals(normalizedPaymentStatus) && !"COMPLETE".equals(targetStatus)) {
            throw new IllegalArgumentException("Orders with PAID payments must remain COMPLETE unless cancelled");
        }

        if (!"PAID".equals(normalizedPaymentStatus) && "COMPLETE".equals(targetStatus)) {
            throw new IllegalArgumentException("Order can be marked COMPLETE only when payment status is PAID");
        }
    }
}
