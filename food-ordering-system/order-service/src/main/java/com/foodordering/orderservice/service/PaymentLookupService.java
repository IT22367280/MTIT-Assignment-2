package com.foodordering.orderservice.service;

import com.foodordering.orderservice.dto.PaymentSummaryResponse;
import com.foodordering.orderservice.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class PaymentLookupService {

    private final RestTemplate restTemplate;
    private final String paymentServiceBaseUrl;

    public PaymentLookupService(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${payment.service.base-url:http://127.0.0.1:8084}") String paymentServiceBaseUrl
    ) {
        this.restTemplate = restTemplateBuilder.build();
        this.paymentServiceBaseUrl = paymentServiceBaseUrl;
    }

    public PaymentSummaryResponse getPaymentById(Long paymentId) {
        try {
            PaymentSummaryResponse payment = restTemplate.getForObject(
                    paymentServiceBaseUrl + "/payments/{id}",
                    PaymentSummaryResponse.class,
                    paymentId
            );

            if (payment == null) {
                throw new IllegalStateException("Payment service returned an empty response");
            }

            return payment;
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ResourceNotFoundException("Payment not found with id " + paymentId);
        } catch (RestClientException exception) {
            throw new IllegalStateException("Payment service is unavailable", exception);
        }
    }
}
