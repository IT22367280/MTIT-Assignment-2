package com.foodordering.orderservice.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;

@Builder
@Schema(name = "OrderApiErrorResponse", description = "Standard error payload returned by the order service")
public record ApiErrorResponse(
        @Schema(description = "Timestamp when the error occurred", example = "2026-03-26T09:09:12.151648")
        LocalDateTime timestamp,
        @Schema(description = "HTTP status code", example = "404")
        int status,
        @Schema(description = "HTTP status reason", example = "Not Found")
        String error,
        @Schema(description = "Human-readable error message", example = "Customer not found with id 999")
        String message,
        @Schema(description = "Request path that caused the error", example = "/orders/999")
        String path,
        @Schema(description = "Validation errors keyed by field name", example = "{\"status\":\"Status must be one of: PENDING, RECEIVED, COMPLETE, CANCELLED\"}")
        Map<String, String> validationErrors
) {
}
