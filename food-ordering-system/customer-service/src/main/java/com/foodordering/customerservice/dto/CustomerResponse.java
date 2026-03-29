package com.foodordering.customerservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(name = "CustomerResponse", description = "Customer details returned by the customer service")
public record CustomerResponse(
        @Schema(description = "Customer identifier", example = "1")
        Long id,
        @Schema(description = "Customer full name", example = "Alice Johnson")
        String fullName,
        @Schema(description = "Customer email address", example = "alice.johnson@example.com")
        String email,
        @Schema(description = "Customer phone number", example = "+1 202-555-0111")
        String phone,
        @Schema(description = "Customer delivery address", example = "123 Main Street, Springfield")
        String address
) {
}
