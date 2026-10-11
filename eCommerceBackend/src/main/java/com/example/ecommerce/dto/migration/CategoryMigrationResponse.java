package com.example.ecommerce.dto.migration;

import java.util.List;

public record CategoryMigrationResponse(
        List<CategoryMigrationDto> categories,
        Integer total
) {
}
