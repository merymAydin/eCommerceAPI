package com.example.ecommerce.dto.response;

public record LoginResponse(
        String token,
        UserResponse user
) {}