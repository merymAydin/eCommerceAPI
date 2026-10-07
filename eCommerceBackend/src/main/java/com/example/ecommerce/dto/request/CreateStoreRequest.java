package com.example.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateStoreRequest(
        @NotBlank String storeName,
        @NotBlank String phone,
        @NotBlank String taxNo,
        @NotBlank String bankAccount
) {
}
