package com.example.ecommerce.dto.response;

public record SavedCardResponse(
        String id,
        String brand,
        String last4,
        Long expMonth,
        Long expYear
) {
}