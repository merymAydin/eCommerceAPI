package com.example.ecommerce.dto.request;

import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.entity.Payment;

import java.time.LocalDateTime;
import java.util.List;

public record OrderCreateRequest(
                                 String username,
                                 String email,
                                 OrderStatus status,
                                 List<OrderItem> orderItems,
                                 List<Payment> payments,
                                 String shippingAddress,
                                 String shippingCity,
                                 String shippingDistrict,
                                 String shippingPostalCode,
                                 LocalDateTime createdAt
                                 ) {
}
