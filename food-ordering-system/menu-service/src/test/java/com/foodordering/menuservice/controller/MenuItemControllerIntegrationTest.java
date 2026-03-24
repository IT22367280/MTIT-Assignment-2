package com.foodordering.menuservice.controller;

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
import com.foodordering.menuservice.dto.MenuItemRequest;
import com.foodordering.menuservice.dto.MenuItemResponse;
import com.foodordering.menuservice.exception.GlobalExceptionHandler;
import com.foodordering.menuservice.exception.ResourceNotFoundException;
import com.foodordering.menuservice.service.MenuItemService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MenuItemController.class)
@Import(GlobalExceptionHandler.class)
class MenuItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MenuItemService menuItemService;

    @Test
    void getAllMenuItemsReturnsMenuItems() throws Exception {
        when(menuItemService.getAllMenuItems()).thenReturn(List.of(
                new MenuItemResponse(1L, "Chicken Burger", "Burger", 1250.00, true),
                new MenuItemResponse(2L, "Cheese Pizza", "Pizza", 2200.00, true),
                new MenuItemResponse(3L, "Iced Coffee", "Beverage", 650.00, true),
                new MenuItemResponse(4L, "Veggie Wrap", "Wrap", 980.00, true),
                new MenuItemResponse(5L, "French Fries", "Sides", 550.00, true)
        ));

        mockMvc.perform(get("/menu-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].itemName").value("Chicken Burger"))
                .andExpect(jsonPath("$[1].itemName").value("Cheese Pizza"))
                .andExpect(jsonPath("$[2].itemName").value("Iced Coffee"))
                .andExpect(jsonPath("$[4].itemName").value("French Fries"));
    }

    @Test
    void getMenuItemByIdReturnsMenuItem() throws Exception {
        when(menuItemService.getMenuItemById(1L))
                .thenReturn(new MenuItemResponse(1L, "Chicken Burger", "Burger", 1250.00, true));

        mockMvc.perform(get("/menu-items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.category").value("Burger"))
                .andExpect(jsonPath("$.price").value(1250.0));
    }

    @Test
    void createMenuItemReturnsCreatedMenuItem() throws Exception {
        MenuItemRequest request = new MenuItemRequest(
                "Veg Wrap",
                "Wrap",
                990.00,
                true
        );

        when(menuItemService.createMenuItem(any(MenuItemRequest.class)))
                .thenReturn(new MenuItemResponse(6L, "Veg Wrap", "Wrap", 990.00, true));

        mockMvc.perform(post("/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/menu-items/6"))
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.itemName").value("Veg Wrap"))
                .andExpect(jsonPath("$.category").value("Wrap"))
                .andExpect(jsonPath("$.price").value(990.0))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void updateMenuItemReturnsUpdatedMenuItem() throws Exception {
        MenuItemRequest request = new MenuItemRequest(
                "Chicken Burger Deluxe",
                "Burger",
                1450.00,
                false
        );

        when(menuItemService.updateMenuItem(eq(1L), any(MenuItemRequest.class)))
                .thenReturn(new MenuItemResponse(1L, "Chicken Burger Deluxe", "Burger", 1450.00, false));

        mockMvc.perform(put("/menu-items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.itemName").value("Chicken Burger Deluxe"))
                .andExpect(jsonPath("$.price").value(1450.0))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void deleteMenuItemReturnsNoContent() throws Exception {
        doNothing().when(menuItemService).deleteMenuItem(1L);

        mockMvc.perform(delete("/menu-items/1"))
                .andExpect(status().isNoContent());

        verify(menuItemService).deleteMenuItem(1L);
    }

    @Test
    void getMenuItemByIdReturnsNotFoundForMissingMenuItem() throws Exception {
        when(menuItemService.getMenuItemById(999L))
                .thenThrow(new ResourceNotFoundException("Menu item not found with id 999"));

        mockMvc.perform(get("/menu-items/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Menu item not found with id 999"))
                .andExpect(jsonPath("$.path").value("/menu-items/999"));
    }

    @Test
    void createMenuItemReturnsValidationErrorsForInvalidPayload() throws Exception {
        MenuItemRequest request = new MenuItemRequest(
                "",
                "",
                -1.0,
                null
        );

        mockMvc.perform(post("/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors", hasKey("itemName")))
                .andExpect(jsonPath("$.validationErrors", hasKey("category")))
                .andExpect(jsonPath("$.validationErrors", hasKey("price")))
                .andExpect(jsonPath("$.validationErrors", hasKey("available")));
    }
}
