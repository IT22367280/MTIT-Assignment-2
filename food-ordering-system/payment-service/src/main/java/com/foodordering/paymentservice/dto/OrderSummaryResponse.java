package com.foodordering.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderSummaryResponse(
        Long id,
        Double totalAmount
) {
}
