package com.foodordering.paymentservice.controller;

import com.foodordering.paymentservice.dto.PaymentRequest;
import com.foodordering.paymentservice.dto.PaymentResponse;
import com.foodordering.paymentservice.dto.PaymentStatusUpdateRequest;
import com.foodordering.paymentservice.exception.ApiErrorResponse;
import com.foodordering.paymentservice.service.PaymentService;
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
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Process and track payment records for customer orders")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Create payment", description = "Creates a payment entry for an existing order.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Payment created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid payment payload or business rule violation",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest paymentRequest) {
        PaymentResponse createdPayment = paymentService.createPayment(paymentRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdPayment.id())
                .toUri();

        return ResponseEntity.created(location).body(createdPayment);
    }

    @GetMapping
    @Operation(summary = "List payments", description = "Returns all payment records currently stored by the service.")
    @ApiResponse(
            responseCode = "200",
            description = "Payments retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = PaymentResponse.class))
            )
    )
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Returns the payment for the supplied identifier.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Payment not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<PaymentResponse> getPaymentById(
            @Parameter(description = "Payment identifier", example = "1") @PathVariable Long id
    ) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update payment status", description = "Updates the settlement status of an existing payment.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment status updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid payment status payload or business rule violation",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Payment not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<PaymentResponse> updatePaymentStatus(
            @Parameter(description = "Payment identifier", example = "1") @PathVariable Long id,
            @Valid @RequestBody PaymentStatusUpdateRequest paymentStatusUpdateRequest
    ) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(id, paymentStatusUpdateRequest));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete payment", description = "Deletes the payment with the supplied identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Payment deleted successfully", content = @Content),
            @ApiResponse(
                    responseCode = "404",
                    description = "Payment not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<Void> deletePayment(
            @Parameter(description = "Payment identifier", example = "1") @PathVariable Long id
    ) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
}
