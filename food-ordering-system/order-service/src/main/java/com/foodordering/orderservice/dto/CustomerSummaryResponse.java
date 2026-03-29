package com.foodordering.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CustomerSummaryResponse", description = "Minimal customer data resolved from the customer service")
public record CustomerSummaryResponse(
        @Schema(description = "Customer identifier", example = "2")
        Long id,
        @Schema(description = "Customer full name", example = "Brian Lee")
        String fullName
) {
}
