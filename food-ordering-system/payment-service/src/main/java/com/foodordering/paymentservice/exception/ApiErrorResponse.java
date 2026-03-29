package com.foodordering.paymentservice.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;

@Builder
@Schema(name = "PaymentApiErrorResponse", description = "Standard error payload returned by the payment service")
public record ApiErrorResponse(
        @Schema(description = "Timestamp when the error occurred", example = "2026-03-26T09:09:12.249538")
        LocalDateTime timestamp,
        @Schema(description = "HTTP status code", example = "400")
        int status,
        @Schema(description = "HTTP status reason", example = "Bad Request")
        String error,
        @Schema(description = "Human-readable error message", example = "CARD payments must use PAID status")
        String message,
        @Schema(description = "Request path that caused the error", example = "/payments")
        String path,
        @Schema(description = "Validation errors keyed by field name", example = "{\"paymentStatus\":\"Payment status is required\"}")
        Map<String, String> validationErrors
) {
}
