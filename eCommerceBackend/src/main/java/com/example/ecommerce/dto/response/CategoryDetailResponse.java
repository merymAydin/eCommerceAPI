package com.example.ecommerce.dto.response;

import com.example.ecommerce.entity.Gender;

import java.math.BigDecimal;
import java.util.List;

public record CategoryDetailResponse(
        Long id,
        String code,
        String title,
        String img,
        BigDecimal rating,
        Gender gender,
        List<ProductResponse> products
) {}
