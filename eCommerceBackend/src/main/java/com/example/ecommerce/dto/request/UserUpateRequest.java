package com.example.ecommerce.dto.request;

import com.example.ecommerce.entity.Role;

public record UserUpateRequest(
        Long roleId,
        String username,
                               String email,
                               Role role) {
}
