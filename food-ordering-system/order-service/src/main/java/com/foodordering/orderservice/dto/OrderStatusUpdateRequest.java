package com.foodordering.orderservice.dto;

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
public class OrderStatusUpdateRequest {

    @NotBlank(message = "Status is required")
    @Size(max = 50, message = "Status must not exceed 50 characters")
    @Pattern(
            regexp = "(?i)^(PENDING|RECEIVED|COMPLETE|CANCELLED)$",
            message = "Status must be one of: PENDING, RECEIVED, COMPLETE, CANCELLED"
    )
    private String status;
}
