package com.foodordering.menuservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(name = "MenuItemResponse", description = "Menu item details returned by the menu service")
public record MenuItemResponse(
        @Schema(description = "Menu item identifier", example = "1")
        Long id,
        @Schema(description = "Display name of the menu item", example = "Chicken Burger")
        String itemName,
        @Schema(description = "Menu category", example = "Burger")
        String category,
        @Schema(description = "Price in local currency", example = "1250.00")
        Double price,
        @Schema(description = "Availability status of the menu item", example = "true")
        Boolean available
) {
}
