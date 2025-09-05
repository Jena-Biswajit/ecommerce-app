package com.example.customer.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
        @NotBlank String line1,
        String line2,
        @NotBlank String city,
        String state,
        @NotBlank String postalCode,
        @NotBlank String country,
        Boolean makeDefault
) {}
