package com.example.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank String shippingAddress,
        @NotBlank String shippingCity,
        @NotBlank String shippingDistrict,
        String shippingPostalCode,
        @NotEmpty List<CreateOrderItemRequest> orderItems
                                 ) {
}


