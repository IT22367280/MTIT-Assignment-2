package com.foodordering.paymentservice.service;

import com.foodordering.paymentservice.dto.OrderPaymentLinkRequest;
import com.foodordering.paymentservice.dto.OrderSummaryResponse;
import com.foodordering.paymentservice.dto.OrderStatusUpdateRequest;
import com.foodordering.paymentservice.exception.ResourceNotFoundException;
import org.springframework.http.HttpEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class OrderLookupService {

    private final RestTemplate restTemplate;
    private final String orderServiceBaseUrl;

    public OrderLookupService(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${order.service.base-url:http://127.0.0.1:8083}") String orderServiceBaseUrl
    ) {
        this.restTemplate = restTemplateBuilder.build();
        this.orderServiceBaseUrl = orderServiceBaseUrl;
    }

    public OrderSummaryResponse getOrderById(Long orderId) {
        try {
            OrderSummaryResponse order = restTemplate.getForObject(
                    orderServiceBaseUrl + "/orders/{id}",
                    OrderSummaryResponse.class,
                    orderId
            );

            if (order == null) {
                throw new IllegalStateException("Order service returned an empty response");
            }

            return order;
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ResourceNotFoundException("Order not found with id " + orderId);
        } catch (RestClientException exception) {
            throw new IllegalStateException("Order service is unavailable", exception);
        }
    }

    public void linkPaymentToOrder(Long orderId, Long paymentId) {
        updateOrderPayment(orderId, paymentId);
    }

    public void unlinkPaymentFromOrder(Long orderId) {
        updateOrderPayment(orderId, null);
    }

    public void updateOrderStatus(Long orderId, String status) {
        try {
            restTemplate.put(
                    orderServiceBaseUrl + "/orders/{id}/status",
                    new HttpEntity<>(new OrderStatusUpdateRequest(status)),
                    orderId
            );
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ResourceNotFoundException("Order not found with id " + orderId);
        } catch (RestClientException exception) {
            throw new IllegalStateException("Order service is unavailable", exception);
        }
    }

    private void updateOrderPayment(Long orderId, Long paymentId) {
        try {
            restTemplate.put(
                    orderServiceBaseUrl + "/orders/{id}/payment",
                    new HttpEntity<>(new OrderPaymentLinkRequest(paymentId)),
                    orderId
            );
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ResourceNotFoundException("Order not found with id " + orderId);
        } catch (RestClientException exception) {
            throw new IllegalStateException("Order service is unavailable", exception);
        }
    }
}
