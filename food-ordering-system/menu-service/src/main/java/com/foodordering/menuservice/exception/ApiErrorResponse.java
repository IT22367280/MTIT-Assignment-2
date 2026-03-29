package com.foodordering.menuservice.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;

@Builder
@Schema(name = "MenuApiErrorResponse", description = "Standard error payload returned by the menu service")
public record ApiErrorResponse(
        @Schema(description = "Timestamp when the error occurred", example = "2026-03-26T09:26:10.928851")
        LocalDateTime timestamp,
        @Schema(description = "HTTP status code", example = "404")
        int status,
        @Schema(description = "HTTP status reason", example = "Not Found")
        String error,
        @Schema(description = "Human-readable error message", example = "Menu item not found with id 999")
        String message,
        @Schema(description = "Request path that caused the error", example = "/menu-items/999")
        String path,
        @Schema(description = "Validation errors keyed by field name", example = "{\"price\":\"Price must be greater than 0\"}")
        Map<String, String> validationErrors
) {
}
