package com.foodordering.orderservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "OrderPaymentUpdateRequest", description = "Payload used to link or unlink a payment from an order")
public class OrderPaymentUpdateRequest {

    @Schema(description = "Payment identifier to link. Send null to clear the link.", example = "9", nullable = true)
    @Min(value = 1, message = "Payment ID must be greater than 0")
    private Long paymentId;
}
