package com.foodordering.orderservice.service;

import com.foodordering.orderservice.dto.CustomerSummaryResponse;
import com.foodordering.orderservice.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class CustomerLookupService {

    private final RestTemplate restTemplate;
    private final String customerServiceBaseUrl;

    public CustomerLookupService(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${customer.service.base-url:http://127.0.0.1:8081}") String customerServiceBaseUrl
    ) {
        this.restTemplate = restTemplateBuilder.build();
        this.customerServiceBaseUrl = customerServiceBaseUrl;
    }

    public CustomerSummaryResponse getCustomerById(Long customerId) {
        try {
            CustomerSummaryResponse customer = restTemplate.getForObject(
                    customerServiceBaseUrl + "/customers/{id}",
                    CustomerSummaryResponse.class,
                    customerId
            );

            if (customer == null) {
                throw new IllegalStateException("Customer service returned an empty response");
            }

            return customer;
        } catch (HttpClientErrorException.NotFound exception) {
            throw new ResourceNotFoundException("Customer not found with id " + customerId);
        } catch (RestClientException exception) {
            throw new IllegalStateException("Customer service is unavailable", exception);
        }
    }
}
