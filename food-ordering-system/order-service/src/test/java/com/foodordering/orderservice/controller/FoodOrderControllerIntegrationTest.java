package com.foodordering.orderservice.controller;

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
import com.foodordering.orderservice.dto.FoodOrderRequest;
import com.foodordering.orderservice.dto.FoodOrderResponse;
import com.foodordering.orderservice.dto.OrderPaymentUpdateRequest;
import com.foodordering.orderservice.dto.OrderStatusUpdateRequest;
import com.foodordering.orderservice.exception.GlobalExceptionHandler;
import com.foodordering.orderservice.exception.ResourceNotFoundException;
import com.foodordering.orderservice.service.FoodOrderService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FoodOrderController.class)
@Import(GlobalExceptionHandler.class)
class FoodOrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FoodOrderService foodOrderService;

    @Test
    void createOrderReturnsCreatedOrder() throws Exception {
        FoodOrderRequest request = new FoodOrderRequest(
                2L,
                "Cheese Pizza",
                1,
                2200.00,
                "pending"
        );

        when(foodOrderService.createOrder(any(FoodOrderRequest.class))).thenReturn(
                new FoodOrderResponse(6L, 2L, "Brian Lee", "Cheese Pizza", 1, 2200.00, null, "PENDING")
        );

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/orders/6"))
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.customerId").value(2))
                .andExpect(jsonPath("$.customerName").value("Brian Lee"))
                .andExpect(jsonPath("$.itemName").value("Cheese Pizza"))
                .andExpect(jsonPath("$.quantity").value(1))
                .andExpect(jsonPath("$.totalAmount").value(2200.0))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getAllOrdersReturnsOrders() throws Exception {
        when(foodOrderService.getAllOrders()).thenReturn(List.of(
                new FoodOrderResponse(1L, 1L, "Alice Johnson", "Chicken Burger", 2, 2500.00, 10L, "PENDING"),
                new FoodOrderResponse(2L, 2L, "Brian Lee", "Cheese Pizza", 1, 2200.00, null, "RECEIVED"),
                new FoodOrderResponse(3L, 3L, "Catherine Smith", "Iced Coffee", 3, 1950.00, 12L, "COMPLETE"),
                new FoodOrderResponse(4L, 4L, "Daniel Perez", "Veggie Wrap", 2, 1960.00, null, "CANCELLED"),
                new FoodOrderResponse(5L, 5L, "Emma Wilson", "French Fries", 1, 550.00, 15L, "PENDING")
        ));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].customerId").value(1))
                .andExpect(jsonPath("$[0].customerName").value("Alice Johnson"))
                .andExpect(jsonPath("$[0].itemName").value("Chicken Burger"))
                .andExpect(jsonPath("$[0].quantity").value(2))
                .andExpect(jsonPath("$[0].totalAmount").value(2500.0))
                .andExpect(jsonPath("$[0].paymentId").value(10))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].status").value("RECEIVED"))
                .andExpect(jsonPath("$[2].status").value("COMPLETE"))
                .andExpect(jsonPath("$[3].status").value("CANCELLED"));
    }

    @Test
    void getOrderByIdReturnsOrder() throws Exception {
        when(foodOrderService.getOrderById(1L))
                .thenReturn(new FoodOrderResponse(1L, 1L, "Alice Johnson", "Chicken Burger", 2, 2500.00, 11L, "PENDING"));

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.customerName").value("Alice Johnson"))
                .andExpect(jsonPath("$.itemName").value("Chicken Burger"))
                .andExpect(jsonPath("$.paymentId").value(11));
    }

    @Test
    void updateOrderPaymentReturnsUpdatedOrder() throws Exception {
        OrderPaymentUpdateRequest request = new OrderPaymentUpdateRequest(9L);

        when(foodOrderService.updateOrderPayment(eq(1L), any(OrderPaymentUpdateRequest.class)))
                .thenReturn(new FoodOrderResponse(1L, 1L, "Alice Johnson", "Chicken Burger", 2, 2500.00, 9L, "PENDING"));

        mockMvc.perform(put("/orders/1/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.paymentId").value(9))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void updateOrderStatusReturnsUpdatedOrder() throws Exception {
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest("complete");

        when(foodOrderService.updateOrderStatus(eq(1L), any(OrderStatusUpdateRequest.class)))
                .thenReturn(new FoodOrderResponse(1L, 1L, "Alice Johnson", "Chicken Burger", 2, 2500.00, 9L, "COMPLETE"));

        mockMvc.perform(put("/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerName").value("Alice Johnson"))
                .andExpect(jsonPath("$.status").value("COMPLETE"));
    }

    @Test
    void deleteOrderReturnsNoContent() throws Exception {
        doNothing().when(foodOrderService).deleteOrder(1L);

        mockMvc.perform(delete("/orders/1"))
                .andExpect(status().isNoContent());

        verify(foodOrderService).deleteOrder(1L);
    }

    @Test
    void getOrderByIdReturnsNotFoundForMissingOrder() throws Exception {
        when(foodOrderService.getOrderById(999L))
                .thenThrow(new ResourceNotFoundException("Order not found with id 999"));

        mockMvc.perform(get("/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Order not found with id 999"))
                .andExpect(jsonPath("$.path").value("/orders/999"));
    }

    @Test
    void createOrderReturnsValidationErrorsForInvalidPayload() throws Exception {
        FoodOrderRequest request = new FoodOrderRequest(
                0L,
                "",
                0,
                -1.0,
                ""
        );

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors", hasKey("customerId")))
                .andExpect(jsonPath("$.validationErrors", hasKey("itemName")))
                .andExpect(jsonPath("$.validationErrors", hasKey("quantity")))
                .andExpect(jsonPath("$.validationErrors", hasKey("totalAmount")))
                .andExpect(jsonPath("$.validationErrors", hasKey("status")));
    }

    @Test
    void createOrderReturnsNotFoundForMissingCustomer() throws Exception {
        FoodOrderRequest request = new FoodOrderRequest(
                999L,
                "Cheese Pizza",
                1,
                2200.00,
                "PENDING"
        );

        when(foodOrderService.createOrder(any(FoodOrderRequest.class)))
                .thenThrow(new ResourceNotFoundException("Customer not found with id 999"));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found with id 999"));
    }

    @Test
    void updateOrderStatusReturnsValidationErrorForInvalidStatus() throws Exception {
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest("confirmed");

        mockMvc.perform(put("/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors", hasKey("status")))
                .andExpect(jsonPath("$.validationErrors.status")
                        .value("Status must be one of: PENDING, RECEIVED, COMPLETE, CANCELLED"));
    }
}
