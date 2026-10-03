package com.example.ecommerce.dto.request;

import com.example.ecommerce.entity.Role;

public record UserCreateRequest(
        String username,
        String email,
        Long roleId
) {
}
