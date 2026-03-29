package com.foodordering.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(name = "FoodOrderResponse", description = "Order details returned by the order service")
public record FoodOrderResponse(
        @Schema(description = "Order identifier", example = "6")
        Long id,
        @Schema(description = "Customer identifier", example = "2")
        Long customerId,
        @Schema(description = "Resolved customer name", example = "Brian Lee")
        String customerName,
        @Schema(description = "Name of the ordered item", example = "Cheese Pizza")
        String itemName,
        @Schema(description = "Number of items ordered", example = "1")
        Integer quantity,
        @Schema(description = "Final order total", example = "2200.00")
        Double totalAmount,
        @Schema(description = "Linked payment identifier, if a payment has been created for the order", example = "9", nullable = true)
        Long paymentId,
        @Schema(description = "Current order status", example = "PENDING")
        String status
) {
}
