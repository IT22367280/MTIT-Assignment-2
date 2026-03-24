package com.foodordering.orderservice.service;

import com.foodordering.orderservice.dto.FoodOrderRequest;
import com.foodordering.orderservice.dto.FoodOrderResponse;
import com.foodordering.orderservice.dto.OrderStatusUpdateRequest;
import com.foodordering.orderservice.exception.ResourceNotFoundException;
import com.foodordering.orderservice.model.FoodOrder;
import com.foodordering.orderservice.repository.FoodOrderRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FoodOrderServiceImpl implements FoodOrderService {

    private final FoodOrderRepository foodOrderRepository;
    private final CustomerLookupService customerLookupService;

    @Override
    public FoodOrderResponse createOrder(FoodOrderRequest foodOrderRequest) {
        customerLookupService.getCustomerById(foodOrderRequest.getCustomerId());

        FoodOrder foodOrder = FoodOrder.builder()
                .customerId(foodOrderRequest.getCustomerId())
                .itemName(foodOrderRequest.getItemName())
                .quantity(foodOrderRequest.getQuantity())
                .totalAmount(foodOrderRequest.getTotalAmount())
                .status(normalizeStatus(foodOrderRequest.getStatus()))
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
    public FoodOrderResponse updateOrderStatus(Long id, OrderStatusUpdateRequest orderStatusUpdateRequest) {
        FoodOrder foodOrder = findOrderById(id);
        foodOrder.setStatus(normalizeStatus(orderStatusUpdateRequest.getStatus()));

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
        String customerName = customerLookupService.getCustomerById(foodOrder.getCustomerId()).fullName();

        return FoodOrderResponse.builder()
                .id(foodOrder.getId())
                .customerId(foodOrder.getCustomerId())
                .customerName(customerName)
                .itemName(foodOrder.getItemName())
                .quantity(foodOrder.getQuantity())
                .totalAmount(foodOrder.getTotalAmount())
                .status(foodOrder.getStatus())
                .build();
    }

    private String normalizeStatus(String status) {
        return status.trim().toUpperCase();
    }
}
