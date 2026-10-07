package com.example.ecommerce.dto.response;

import jakarta.validation.constraints.NotBlank;

public record StoreResponse(
        Long userId,
         String storeName,
        String phone,
         String taxNo,
       String bankAccount
) {
}
