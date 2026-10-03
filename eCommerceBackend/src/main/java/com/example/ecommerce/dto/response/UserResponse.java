package com.example.ecommerce.dto.response;

import com.example.ecommerce.entity.Role;

public record UserResponse(String username,
                           String email,
                           String roleName) {
}
