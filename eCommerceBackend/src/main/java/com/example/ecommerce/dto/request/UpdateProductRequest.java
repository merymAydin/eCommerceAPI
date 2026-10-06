package com.example.ecommerce.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UpdateProductRequest(
        @NotNull
        Long categoryId,
        @NotBlank
        @Size(min = 3)
        String name,

        @NotBlank
        @Size(min = 3)
        String description,

        @NotNull
        @DecimalMin("0.0")
        BigDecimal price,

        @PositiveOrZero
        int stock,

        @NotBlank
        String imageUrl
) {
}
