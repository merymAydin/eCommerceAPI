package com.example.ecommerce.dto.migration;

import com.example.ecommerce.entity.Gender;

import java.math.BigDecimal;
import java.util.List;

public record CategoryMigrationDto(
        Long id,
        String code,
        String title,
        String img,
        BigDecimal rating,
        String gender
) {}
