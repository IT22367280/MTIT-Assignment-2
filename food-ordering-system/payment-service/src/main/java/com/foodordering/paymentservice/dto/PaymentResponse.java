package com.foodordering.paymentservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(name = "PaymentResponse", description = "Payment details returned by the payment service")
public record PaymentResponse(
        @Schema(description = "Payment identifier", example = "5")
        Long id,
        @Schema(description = "Associated order identifier", example = "2")
        Long orderId,
        @Schema(description = "Payment amount", example = "2200.00")
        Double amount,
        @Schema(description = "Payment method used by the customer", example = "CASH")
        String paymentMethod,
        @Schema(description = "Current payment settlement status", example = "ON_DELIVERY")
        String paymentStatus
) {
}
