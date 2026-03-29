package com.foodordering.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "OrderStatusUpdateRequest", description = "Payload used to update the status of an order")
public class OrderStatusUpdateRequest {

    @Schema(description = "Updated order status", example = "COMPLETE", allowableValues = {"PENDING", "RECEIVED", "COMPLETE", "CANCELLED"})
    @NotBlank(message = "Status is required")
    @Size(max = 50, message = "Status must not exceed 50 characters")
    @Pattern(
            regexp = "(?i)^(PENDING|RECEIVED|COMPLETE|CANCELLED)$",
            message = "Status must be one of: PENDING, RECEIVED, COMPLETE, CANCELLED"
    )
    private String status;
}
