package com.foodordering.customerservice.dto;

import lombok.Builder;

@Builder
public record CustomerResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String address
) {
}
