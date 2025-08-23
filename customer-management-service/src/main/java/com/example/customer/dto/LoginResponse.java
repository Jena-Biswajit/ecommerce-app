package com.example.customer.dto;

public record LoginResponse(Long customerId, String email, String token) {}
