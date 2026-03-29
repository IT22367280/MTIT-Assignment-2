package com.foodordering.customerservice.service;

import com.foodordering.customerservice.dto.CustomerRequest;
import com.foodordering.customerservice.dto.CustomerResponse;
import java.util.List;

public interface CustomerService {

    List<CustomerResponse> getAllCustomers();

    CustomerResponse getCustomerById(Long id);

    CustomerResponse createCustomer(CustomerRequest customerRequest);

    CustomerResponse resolveCustomer(CustomerRequest customerRequest);

    CustomerResponse updateCustomer(Long id, CustomerRequest customerRequest);

    void deleteCustomer(Long id);
}
