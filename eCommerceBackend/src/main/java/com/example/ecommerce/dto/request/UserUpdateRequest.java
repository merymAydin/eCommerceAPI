package com.example.ecommerce.dto.request;
import jakarta.validation.constraints.*;

public record UserUpdateRequest
        (
                @NotBlank String username,
                @NotBlank @Email String email,
                @NotBlank @Size(min = 6) String password
        ) {
}
