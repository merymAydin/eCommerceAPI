package com.example.ecommerce.dto.request;

import com.example.ecommerce.entity.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateCategoryRequest(
        @NotBlank String code,
        @NotBlank @Size(min = 3) String title,
        @NotBlank String img,
        @NotNull BigDecimal rating,
        @NotNull Gender gender
) {
}
