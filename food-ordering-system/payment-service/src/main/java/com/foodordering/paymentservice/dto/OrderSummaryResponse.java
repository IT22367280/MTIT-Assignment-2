package com.foodordering.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "OrderSummaryResponse", description = "Minimal order data resolved from the order service")
public record OrderSummaryResponse(
        @Schema(description = "Order identifier", example = "2")
        Long id,
        @Schema(description = "Final order total", example = "2200.00")
        Double totalAmount,
        @Schema(description = "Linked payment identifier, if the order already has one", example = "9", nullable = true)
        Long paymentId,
        @Schema(description = "Current order status", example = "RECEIVED")
        String status
) {
}
