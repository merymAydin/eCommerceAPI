package com.example.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank String username,
@NotBlank String email,
                                 List<CreateOrderItemRequest> orderItems
                                 ) {
}


