package com.foodordering.orderservice.dto;

import lombok.Builder;

@Builder
public record FoodOrderResponse(
        Long id,
        Long customerId,
        String customerName,
        String itemName,
        Integer quantity,
        Double totalAmount,
        String status
) {
}
