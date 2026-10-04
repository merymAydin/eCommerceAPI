package com.example.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordUpdateRequest(@NotBlank @Size(min = 6) String currentPassword, @NotBlank @Size(min = 6) String newPassword) {
}
