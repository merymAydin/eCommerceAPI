package com.example.ecommerce.dto.request;

import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Store;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateProductRequest(
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
        String imageUrl,

        @NotNull
        Long storeId
) {
}
