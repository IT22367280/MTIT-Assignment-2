package com.foodordering.paymentservice.dto;

import lombok.Builder;

@Builder
public record PaymentResponse(
        Long id,
        Long orderId,
        Double amount,
        String paymentMethod,
        String paymentStatus
) {
}
