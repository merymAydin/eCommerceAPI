package com.example.ecommerce.dto.migration;

import java.util.List;

public record ProductMigrationResponse(
        List<ProductMigrationDto> products,
        Integer total
) {
}
