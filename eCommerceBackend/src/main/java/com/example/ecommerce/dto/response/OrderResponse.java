package com.example.ecommerce.dto.response;

import com.example.ecommerce.entity.OrderStatus;


import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
         String username,
         String email,
         List<OrderItemResponse> orderItems,
         OrderStatus status,
         LocalDateTime createdAt,
         String shippingAddress,
         String shippingCity,
         String shippingDistrict,
         String shippingPostalCode
) {
}
