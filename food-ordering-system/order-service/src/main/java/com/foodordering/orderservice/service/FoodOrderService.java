package com.foodordering.orderservice.service;

import com.foodordering.orderservice.dto.FoodOrderRequest;
import com.foodordering.orderservice.dto.FoodOrderResponse;
import com.foodordering.orderservice.dto.OrderPaymentUpdateRequest;
import com.foodordering.orderservice.dto.OrderStatusUpdateRequest;
import java.util.List;

public interface FoodOrderService {

    FoodOrderResponse createOrder(FoodOrderRequest foodOrderRequest);

    List<FoodOrderResponse> getAllOrders();

    FoodOrderResponse getOrderById(Long id);

    FoodOrderResponse updateOrderPayment(Long id, OrderPaymentUpdateRequest orderPaymentUpdateRequest);

    FoodOrderResponse updateOrderStatus(Long id, OrderStatusUpdateRequest orderStatusUpdateRequest);

    void deleteOrder(Long id);
}
