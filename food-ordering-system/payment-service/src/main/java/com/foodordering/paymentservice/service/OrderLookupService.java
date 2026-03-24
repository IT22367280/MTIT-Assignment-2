package com.foodordering.paymentservice.service;

import com.foodordering.paymentservice.dto.OrderSummaryResponse;
import com.foodordering.paymentservice.exception.ResourceNotFoundException;
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
            @Value("${order.service.base-url:http://localhost:8083}") String orderServiceBaseUrl
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
}
