package com.example.ecommerce.dto.request;

public record UpdateOrderItemsRequest(
        Long productId,
        int quantity
) {
}
