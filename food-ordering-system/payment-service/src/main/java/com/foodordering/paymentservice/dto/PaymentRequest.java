package com.foodordering.paymentservice.dto;

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
public class PaymentRequest {

    @NotNull(message = "Order id is required")
    @Min(value = 1, message = "Order id must be greater than 0")
    private Long orderId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private Double amount;

    @NotBlank(message = "Payment method is required")
    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    @Pattern(
            regexp = "(?i)^(CASH|CARD)$",
            message = "Payment method must be one of: CASH, CARD"
    )
    private String paymentMethod;

    @NotBlank(message = "Payment status is required")
    @Size(max = 50, message = "Payment status must not exceed 50 characters")
    @Pattern(
            regexp = "(?i)^(PAID|ON[ _]DELIVERY)$",
            message = "Payment status must be one of: PAID, ON_DELIVERY"
    )
    private String paymentStatus;
}
