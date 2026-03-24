package com.foodordering.paymentservice.controller;

import static org.hamcrest.Matchers.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodordering.paymentservice.dto.PaymentRequest;
import com.foodordering.paymentservice.dto.PaymentResponse;
import com.foodordering.paymentservice.dto.PaymentStatusUpdateRequest;
import com.foodordering.paymentservice.exception.GlobalExceptionHandler;
import com.foodordering.paymentservice.exception.ResourceNotFoundException;
import com.foodordering.paymentservice.service.PaymentService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PaymentController.class)
@Import(GlobalExceptionHandler.class)
class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void createPaymentReturnsCreatedPayment() throws Exception {
        PaymentRequest request = new PaymentRequest(
                2L,
                2200.00,
                "cash",
                "on delivery"
        );

        when(paymentService.createPayment(any(PaymentRequest.class)))
                .thenReturn(new PaymentResponse(5L, 2L, 2200.00, "CASH", "ON_DELIVERY"));

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/payments/5"))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.orderId").value(2))
                .andExpect(jsonPath("$.amount").value(2200.0))
                .andExpect(jsonPath("$.paymentMethod").value("CASH"))
                .andExpect(jsonPath("$.paymentStatus").value("ON_DELIVERY"));
    }

    @Test
    void getAllPaymentsReturnsPayments() throws Exception {
        when(paymentService.getAllPayments()).thenReturn(List.of(
                new PaymentResponse(1L, 1L, 2500.00, "CARD", "PAID"),
                new PaymentResponse(2L, 2L, 2200.00, "CASH", "ON_DELIVERY"),
                new PaymentResponse(3L, 3L, 1950.00, "CARD", "PAID"),
                new PaymentResponse(4L, 5L, 550.00, "CASH", "PAID")
        ));

        mockMvc.perform(get("/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].orderId").value(1))
                .andExpect(jsonPath("$[0].amount").value(2500.0))
                .andExpect(jsonPath("$[0].paymentMethod").value("CARD"))
                .andExpect(jsonPath("$[0].paymentStatus").value("PAID"))
                .andExpect(jsonPath("$[1].orderId").value(2))
                .andExpect(jsonPath("$[1].paymentMethod").value("CASH"))
                .andExpect(jsonPath("$[1].paymentStatus").value("ON_DELIVERY"))
                .andExpect(jsonPath("$[2].orderId").value(3))
                .andExpect(jsonPath("$[2].paymentMethod").value("CARD"))
                .andExpect(jsonPath("$[2].paymentStatus").value("PAID"))
                .andExpect(jsonPath("$[3].orderId").value(5))
                .andExpect(jsonPath("$[3].paymentMethod").value("CASH"))
                .andExpect(jsonPath("$[3].paymentStatus").value("PAID"));
    }

    @Test
    void getPaymentByIdReturnsPayment() throws Exception {
        when(paymentService.getPaymentById(1L))
                .thenReturn(new PaymentResponse(1L, 1L, 2500.00, "CARD", "PAID"));

        mockMvc.perform(get("/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.paymentMethod").value("CARD"))
                .andExpect(jsonPath("$.paymentStatus").value("PAID"));
    }

    @Test
    void updatePaymentStatusReturnsUpdatedPayment() throws Exception {
        PaymentStatusUpdateRequest request = new PaymentStatusUpdateRequest("paid");

        when(paymentService.updatePaymentStatus(eq(2L), any(PaymentStatusUpdateRequest.class)))
                .thenReturn(new PaymentResponse(2L, 2L, 2200.00, "CASH", "PAID"));

        mockMvc.perform(put("/payments/2/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.paymentStatus").value("PAID"));
    }

    @Test
    void deletePaymentReturnsNoContent() throws Exception {
        doNothing().when(paymentService).deletePayment(1L);

        mockMvc.perform(delete("/payments/1"))
                .andExpect(status().isNoContent());

        verify(paymentService).deletePayment(1L);
    }

    @Test
    void getPaymentByIdReturnsNotFoundForMissingPayment() throws Exception {
        when(paymentService.getPaymentById(999L))
                .thenThrow(new ResourceNotFoundException("Payment not found with id 999"));

        mockMvc.perform(get("/payments/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Payment not found with id 999"))
                .andExpect(jsonPath("$.path").value("/payments/999"));
    }

    @Test
    void createPaymentReturnsValidationErrorsForInvalidPayload() throws Exception {
        PaymentRequest request = new PaymentRequest(
                0L,
                -1.0,
                "",
                ""
        );

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors", hasKey("orderId")))
                .andExpect(jsonPath("$.validationErrors", hasKey("amount")))
                .andExpect(jsonPath("$.validationErrors", hasKey("paymentMethod")))
                .andExpect(jsonPath("$.validationErrors", hasKey("paymentStatus")));
    }

    @Test
    void createPaymentReturnsBadRequestWhenAmountDoesNotMatchOrder() throws Exception {
        PaymentRequest request = new PaymentRequest(
                2L,
                2000.00,
                "CASH",
                "ON DELIVERY"
        );

        when(paymentService.createPayment(any(PaymentRequest.class)))
                .thenThrow(new IllegalArgumentException("Payment amount must match order total amount 2200.0"));

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Payment amount must match order total amount 2200.0"));
    }

    @Test
    void createPaymentReturnsBadRequestForCardOnDeliveryCombination() throws Exception {
        PaymentRequest request = new PaymentRequest(
                3L,
                1950.00,
                "CARD",
                "ON DELIVERY"
        );

        when(paymentService.createPayment(any(PaymentRequest.class)))
                .thenThrow(new IllegalArgumentException("CARD payments must use PAID status"));

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CARD payments must use PAID status"));
    }

    @Test
    void createPaymentReturnsNotFoundForMissingOrder() throws Exception {
        PaymentRequest request = new PaymentRequest(
                999L,
                1000.00,
                "CASH",
                "PAID"
        );

        when(paymentService.createPayment(any(PaymentRequest.class)))
                .thenThrow(new ResourceNotFoundException("Order not found with id 999"));

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order not found with id 999"));
    }

    @Test
    void updatePaymentStatusReturnsValidationErrorForBlankStatus() throws Exception {
        PaymentStatusUpdateRequest request = new PaymentStatusUpdateRequest("");

        mockMvc.perform(put("/payments/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors", hasKey("paymentStatus")))
                .andExpect(jsonPath("$.validationErrors.paymentStatus")
                        .value("Payment status is required"));
    }

    @Test
    void updatePaymentStatusReturnsBadRequestForCardPaymentWithOnDeliveryStatus() throws Exception {
        PaymentStatusUpdateRequest request = new PaymentStatusUpdateRequest("ON DELIVERY");

        when(paymentService.updatePaymentStatus(eq(1L), any(PaymentStatusUpdateRequest.class)))
                .thenThrow(new IllegalArgumentException("CARD payments must use PAID status"));

        mockMvc.perform(put("/payments/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CARD payments must use PAID status"));
    }
}
