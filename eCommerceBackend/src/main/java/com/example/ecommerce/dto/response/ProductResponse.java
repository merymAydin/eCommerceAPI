package com.example.ecommerce.dto.response;

import java.math.BigDecimal;

public record ProductResponse (
        Long categoryId,
        String name,
        String description,
        BigDecimal price,
        int stock,
        String imageUrl,
        Long storeId
){
}
