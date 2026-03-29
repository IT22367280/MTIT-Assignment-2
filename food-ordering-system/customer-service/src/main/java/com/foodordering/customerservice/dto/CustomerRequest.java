package com.foodordering.customerservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CustomerRequest", description = "Payload used to create or update a customer")
public class CustomerRequest {

    @Schema(description = "Customer full name", example = "Alice Johnson")
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @Schema(description = "Customer email address", example = "alice.johnson@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @Schema(description = "Customer phone number", example = "+1 202-555-0111")
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[+0-9\\-\\s()]{7,20}$", message = "Phone must be between 7 and 20 valid characters")
    private String phone;

    @Schema(description = "Customer delivery address", example = "123 Main Street, Springfield")
    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;
}
