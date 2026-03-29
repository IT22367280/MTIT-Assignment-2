package com.foodordering.menuservice.controller;

import com.foodordering.menuservice.dto.MenuItemRequest;
import com.foodordering.menuservice.dto.MenuItemResponse;
import com.foodordering.menuservice.exception.ApiErrorResponse;
import com.foodordering.menuservice.service.MenuItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/menu-items")
@RequiredArgsConstructor
@Tag(name = "Menu Items", description = "Manage the food menu available to customers")
public class MenuItemController {

    private final MenuItemService menuItemService;

    @GetMapping
    @Operation(summary = "List menu items", description = "Returns all menu items currently stored by the service.")
    @ApiResponse(
            responseCode = "200",
            description = "Menu items retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = MenuItemResponse.class))
            )
    )
    public ResponseEntity<List<MenuItemResponse>> getAllMenuItems() {
        return ResponseEntity.ok(menuItemService.getAllMenuItems());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get menu item by ID", description = "Returns a menu item for the supplied identifier.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Menu item retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MenuItemResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Menu item not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<MenuItemResponse> getMenuItemById(
            @Parameter(description = "Menu item identifier", example = "1") @PathVariable Long id
    ) {
        return ResponseEntity.ok(menuItemService.getMenuItemById(id));
    }

    @PostMapping
    @Operation(summary = "Create menu item", description = "Creates a new menu item entry.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Menu item created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MenuItemResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid menu item payload",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<MenuItemResponse> createMenuItem(@Valid @RequestBody MenuItemRequest menuItemRequest) {
        MenuItemResponse createdMenuItem = menuItemService.createMenuItem(menuItemRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdMenuItem.id())
                .toUri();

        return ResponseEntity.created(location).body(createdMenuItem);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update menu item", description = "Updates an existing menu item.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Menu item updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MenuItemResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid menu item payload",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Menu item not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @Parameter(description = "Menu item identifier", example = "1") @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest menuItemRequest
    ) {
        return ResponseEntity.ok(menuItemService.updateMenuItem(id, menuItemRequest));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete menu item", description = "Deletes the menu item with the supplied identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Menu item deleted successfully", content = @Content),
            @ApiResponse(
                    responseCode = "404",
                    description = "Menu item not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<Void> deleteMenuItem(
            @Parameter(description = "Menu item identifier", example = "1") @PathVariable Long id
    ) {
        menuItemService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }
}
