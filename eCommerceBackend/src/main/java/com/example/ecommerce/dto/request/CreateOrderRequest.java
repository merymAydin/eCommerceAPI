package com.example.ecommerce.dto.request;

import com.example.ecommerce.dto.response.OrderItemResponse;
import com.example.ecommerce.dto.response.PaymentResponse;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.OrderStatus;
import com.example.ecommerce.entity.Payment;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;

public record CreateOrderRequest(
                                 String username,
                                  String email,
                                 List<CreateOrderItemRequest> orderItems
                                 ) {
}


