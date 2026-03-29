package com.foodordering.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(name = "FoodOrderRequest", description = "Payload used to create a food order")
public class FoodOrderRequest {

    @Schema(description = "Existing customer identifier", example = "2")
    @NotNull(message = "Customer ID is required")
    @Min(value = 1, message = "Customer ID must be greater than 0")
    private Long customerId;

    @Schema(description = "Name of the ordered item", example = "Cheese Pizza")
    @NotBlank(message = "Item name is required")
    @Size(max = 100, message = "Item name must not exceed 100 characters")
    private String itemName;

    @Schema(description = "Number of items ordered", example = "1")
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private Integer quantity;

    @Schema(description = "Final order total", example = "2200.00")
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than 0")
    private Double totalAmount;

    @Schema(description = "Current order status", example = "PENDING", allowableValues = {"PENDING", "RECEIVED", "COMPLETE", "CANCELLED"})
    @NotBlank(message = "Status is required")
    @Size(max = 50, message = "Status must not exceed 50 characters")
    @Pattern(
            regexp = "(?i)^(PENDING|RECEIVED|COMPLETE|CANCELLED)$",
            message = "Status must be one of: PENDING, RECEIVED, COMPLETE, CANCELLED"
    )
    private String status;
}
