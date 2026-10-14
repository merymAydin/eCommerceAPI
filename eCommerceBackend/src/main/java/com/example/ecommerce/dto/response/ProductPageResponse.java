package com.example.ecommerce.dto.response;

import java.util.List;

public record ProductPageResponse(
        List<ProductResponse> products,
        long total
) {
}
