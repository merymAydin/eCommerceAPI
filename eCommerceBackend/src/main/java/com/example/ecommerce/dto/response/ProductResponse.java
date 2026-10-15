package com.example.ecommerce.dto.response;

import java.math.BigDecimal;

public record ProductResponse (
        Long id,
        Long categoryId,
        String name,
        String description,
        BigDecimal price,
        int stock,
        String imageUrl,
        Long storeId,
        Double rating,
        Integer reviewCount
){
}
