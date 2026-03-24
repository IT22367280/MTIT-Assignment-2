package com.foodordering.paymentservice.dto;

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
public class PaymentStatusUpdateRequest {

    @NotBlank(message = "Payment status is required")
    @Size(max = 50, message = "Payment status must not exceed 50 characters")
    @Pattern(
            regexp = "(?i)^(PAID|ON[ _]DELIVERY)$",
            message = "Payment status must be one of: PAID, ON_DELIVERY"
    )
    private String paymentStatus;
}
