package com.example.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record UpdateOrderRequest(
        @NotBlank String username,
        @NotBlank String email,
        @NotBlank String status,
        List<UpdateOrderItemsRequest> orderItems,
        @NotBlank String shippingAddress,
        @NotBlank String shippingCity,
        @NotBlank String shippingDistrict,
        @NotBlank String shippingPostalCode
) {
}
