package com.foodordering.menuservice.dto;

import lombok.Builder;

@Builder
public record MenuItemResponse(
        Long id,
        String itemName,
        String category,
        Double price,
        Boolean available
) {
}
