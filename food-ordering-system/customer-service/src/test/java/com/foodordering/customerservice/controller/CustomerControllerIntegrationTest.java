package com.foodordering.customerservice.controller;

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
import com.foodordering.customerservice.dto.CustomerRequest;
import com.foodordering.customerservice.dto.CustomerResponse;
import com.foodordering.customerservice.exception.GlobalExceptionHandler;
import com.foodordering.customerservice.exception.ResourceNotFoundException;
import com.foodordering.customerservice.service.CustomerService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CustomerController.class)
@Import(GlobalExceptionHandler.class)
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    @Test
    void getAllCustomersReturnsCustomers() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(List.of(
                new CustomerResponse(1L, "Alice Johnson", "alice.johnson@example.com", "+1 202-555-0111",
                        "123 Main Street, Springfield"),
                new CustomerResponse(2L, "Brian Lee", "brian.lee@example.com", "+1 202-555-0199",
                        "456 Oak Avenue, Riverdale"),
                new CustomerResponse(3L, "Catherine Smith", "catherine.smith@example.com", "+1 202-555-0133",
                        "789 Pine Street, Hillview"),
                new CustomerResponse(4L, "Daniel Perez", "daniel.perez@example.com", "+1 202-555-0175",
                        "22 Lake Road, Brookside"),
                new CustomerResponse(5L, "Emma Wilson", "emma.wilson@example.com", "+1 202-555-0148",
                        "88 Sunset Boulevard, Greenville")
        ));

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].fullName").value("Alice Johnson"))
                .andExpect(jsonPath("$[1].fullName").value("Brian Lee"))
                .andExpect(jsonPath("$[4].fullName").value("Emma Wilson"));
    }

    @Test
    void getCustomerByIdReturnsCustomer() throws Exception {
        when(customerService.getCustomerById(1L)).thenReturn(
                new CustomerResponse(1L, "Alice Johnson", "alice.johnson@example.com", "+1 202-555-0111",
                        "123 Main Street, Springfield")
        );

        mockMvc.perform(get("/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("alice.johnson@example.com"));
    }

    @Test
    void createCustomerReturnsCreatedCustomer() throws Exception {
        CustomerRequest request = new CustomerRequest(
                "Charlie Brown",
                "charlie.brown@example.com",
                "+1 202-555-0107",
                "789 Pine Road, Lakeside"
        );

        when(customerService.createCustomer(any(CustomerRequest.class))).thenReturn(
                new CustomerResponse(6L, "Charlie Brown", "charlie.brown@example.com", "+1 202-555-0107",
                        "789 Pine Road, Lakeside")
        );

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/customers/6"))
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.fullName").value("Charlie Brown"))
                .andExpect(jsonPath("$.email").value("charlie.brown@example.com"));
    }

    @Test
    void updateCustomerReturnsUpdatedCustomer() throws Exception {
        CustomerRequest request = new CustomerRequest(
                "Alice Cooper",
                "alice.cooper@example.com",
                "+1 202-555-0177",
                "321 Updated Street, Springfield"
        );

        when(customerService.updateCustomer(eq(1L), any(CustomerRequest.class))).thenReturn(
                new CustomerResponse(1L, "Alice Cooper", "alice.cooper@example.com", "+1 202-555-0177",
                        "321 Updated Street, Springfield")
        );

        mockMvc.perform(put("/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Alice Cooper"))
                .andExpect(jsonPath("$.email").value("alice.cooper@example.com"));
    }

    @Test
    void deleteCustomerReturnsNoContent() throws Exception {
        doNothing().when(customerService).deleteCustomer(1L);

        mockMvc.perform(delete("/customers/1"))
                .andExpect(status().isNoContent());

        verify(customerService).deleteCustomer(1L);
    }

    @Test
    void getCustomerByIdReturnsNotFoundForMissingCustomer() throws Exception {
        when(customerService.getCustomerById(999L))
                .thenThrow(new ResourceNotFoundException("Customer not found with id 999"));

        mockMvc.perform(get("/customers/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Customer not found with id 999"))
                .andExpect(jsonPath("$.path").value("/customers/999"));
    }

    @Test
    void createCustomerReturnsValidationErrorsForInvalidPayload() throws Exception {
        CustomerRequest request = new CustomerRequest(
                "",
                "bad-email",
                "12",
                ""
        );

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors", hasKey("fullName")))
                .andExpect(jsonPath("$.validationErrors", hasKey("email")))
                .andExpect(jsonPath("$.validationErrors", hasKey("phone")))
                .andExpect(jsonPath("$.validationErrors", hasKey("address")));
    }
}
