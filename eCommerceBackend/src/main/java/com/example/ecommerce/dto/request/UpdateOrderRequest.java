package com.example.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record UpdateOrderRequest(
        List<UpdateOrderItemsRequest> orderItems,
        @NotBlank String shippingAddress,
        @NotBlank String shippingCity,
        @NotBlank String shippingDistrict,
        @NotBlank String shippingPostalCode
) {
}
