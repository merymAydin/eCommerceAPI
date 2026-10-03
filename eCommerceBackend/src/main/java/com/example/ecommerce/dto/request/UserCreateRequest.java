package com.example.ecommerce.dto.request;


import jakarta.validation.constraints.*;

public record UserCreateRequest(
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password,
        @NotNull @Positive Long roleId
        ) {
}
