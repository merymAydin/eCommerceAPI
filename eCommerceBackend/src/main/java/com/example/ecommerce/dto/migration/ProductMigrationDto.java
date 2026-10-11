package com.example.ecommerce.dto.migration;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record ProductMigrationDto(
            Long id,
            String name,
            String description,
            BigDecimal price,
        	Integer stock,
            @JsonProperty("store_id")
            Long storeId,
            @JsonProperty("category_id")
        	Long categoryId,
        	Double rating,
            Integer sellCount,
            List<ProductImageMigrationDto> images
) {
}
