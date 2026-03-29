package com.foodordering.orderservice.controller;

import com.foodordering.orderservice.dto.FoodOrderRequest;
import com.foodordering.orderservice.dto.FoodOrderResponse;
import com.foodordering.orderservice.dto.OrderPaymentUpdateRequest;
import com.foodordering.orderservice.dto.OrderStatusUpdateRequest;
import com.foodordering.orderservice.exception.ApiErrorResponse;
import com.foodordering.orderservice.service.FoodOrderService;
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
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Create, retrieve, update, and delete food orders")
public class FoodOrderController {

    private final FoodOrderService foodOrderService;

    @PostMapping
    @Operation(summary = "Create order", description = "Creates a new order for an existing customer.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Order created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FoodOrderResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid order payload",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<FoodOrderResponse> createOrder(@Valid @RequestBody FoodOrderRequest foodOrderRequest) {
        FoodOrderResponse createdOrder = foodOrderService.createOrder(foodOrderRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdOrder.id())
                .toUri();

        return ResponseEntity.created(location).body(createdOrder);
    }

    @GetMapping
    @Operation(summary = "List orders", description = "Returns all orders currently available in the service.")
    @ApiResponse(
            responseCode = "200",
            description = "Orders retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = FoodOrderResponse.class))
            )
    )
    public ResponseEntity<List<FoodOrderResponse>> getAllOrders() {
        return ResponseEntity.ok(foodOrderService.getAllOrders());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Returns the order for the supplied identifier.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Order retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FoodOrderResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<FoodOrderResponse> getOrderById(
            @Parameter(description = "Order identifier", example = "1") @PathVariable Long id
    ) {
        return ResponseEntity.ok(foodOrderService.getOrderById(id));
    }

    @PutMapping("/{id}/payment")
    @Operation(summary = "Link order payment", description = "Stores or clears the payment identifier associated with an existing order.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Order payment link updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FoodOrderResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid payment link payload",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<FoodOrderResponse> updateOrderPayment(
            @Parameter(description = "Order identifier", example = "1") @PathVariable Long id,
            @Valid @RequestBody OrderPaymentUpdateRequest orderPaymentUpdateRequest
    ) {
        return ResponseEntity.ok(foodOrderService.updateOrderPayment(id, orderPaymentUpdateRequest));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update order status", description = "Updates the lifecycle status of an existing order.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Order status updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FoodOrderResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid order status payload",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<FoodOrderResponse> updateOrderStatus(
            @Parameter(description = "Order identifier", example = "1") @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest orderStatusUpdateRequest
    ) {
        return ResponseEntity.ok(foodOrderService.updateOrderStatus(id, orderStatusUpdateRequest));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete order", description = "Deletes the order with the supplied identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Order deleted successfully", content = @Content),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<Void> deleteOrder(
            @Parameter(description = "Order identifier", example = "1") @PathVariable Long id
    ) {
        foodOrderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
