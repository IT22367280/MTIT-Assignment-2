package com.foodordering.menuservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "MenuItemRequest", description = "Payload used to create or update a menu item")
public class MenuItemRequest {

    @Schema(description = "Display name of the menu item", example = "Chicken Burger")
    @NotBlank(message = "Item name is required")
    @Size(max = 100, message = "Item name must not exceed 100 characters")
    private String itemName;

    @Schema(description = "Menu category", example = "Burger")
    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @Schema(description = "Price in local currency", example = "1250.00")
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private Double price;

    @Schema(description = "Availability status of the menu item", example = "true")
    @NotNull(message = "Availability is required")
    private Boolean available;
}
