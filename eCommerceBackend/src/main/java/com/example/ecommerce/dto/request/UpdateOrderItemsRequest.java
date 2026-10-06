package com.example.ecommerce.dto.request;



import java.math.BigDecimal;

public record UpdateOrderItemsRequest(
        Long orderId,
        Long productId,
        Long storeId,
        int quantity,
        BigDecimal unitPrice
) {
}
