package com.example.ecommerce.dto.response;


import java.math.BigDecimal;

public record OrderItemResponse(
         Long orderId,
         Long productId,
         Long storeId,
         int quantity,
        BigDecimal unitPrice
) {
}



