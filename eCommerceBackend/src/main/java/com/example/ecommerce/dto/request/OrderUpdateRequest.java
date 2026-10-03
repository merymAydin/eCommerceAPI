package com.example.ecommerce.dto.request;

import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.entity.Payment;

import java.time.LocalDateTime;
import java.util.List;

public record OrderUpdateRequest(
        OrderStatus status,
        List<OrderItem> orderItems,
        String shippingAddress,
        String shippingCity,
        String shippingDistrict,
        String shippingPostalCode
) {
}
