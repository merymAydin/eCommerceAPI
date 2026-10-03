package com.example.ecommerce.service.interfaces;

import com.example.ecommerce.dto.request.LoginRequest;
import com.example.ecommerce.dto.request.UserCreateRequest;
import com.example.ecommerce.dto.request.UserUpdateRequest;
import com.example.ecommerce.dto.response.UserResponse;

public interface UserService {
    UserResponse createUser(UserCreateRequest userCreateRequest);
    UserResponse updateUser(Long id,UserUpdateRequest userUpateRequest);
    UserResponse login(LoginRequest request);
}
