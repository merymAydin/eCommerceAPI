package com.example.ecommerce.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateOrderItemRequest(
         @NotNull Long productId,
         @Positive int quantity
) {
}
