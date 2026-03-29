package com.foodordering.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "PaymentSummaryResponse", description = "Minimal payment data resolved from the payment service")
public record PaymentSummaryResponse(
        @Schema(description = "Payment identifier", example = "9")
        Long id,
        @Schema(description = "Current payment settlement status", example = "PAID")
        String paymentStatus
) {
}
