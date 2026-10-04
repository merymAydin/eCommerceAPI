package com.example.ecommerce.service.interfaces;

import com.example.ecommerce.dto.request.LoginRequest;
import com.example.ecommerce.dto.request.PasswordUpdateRequest;
import com.example.ecommerce.dto.request.UserCreateRequest;
import com.example.ecommerce.dto.request.UserUpdateRequest;
import com.example.ecommerce.dto.response.LoginResponse;
import com.example.ecommerce.dto.response.UserResponse;
import com.example.ecommerce.entity.User;

public interface UserService {
    UserResponse createUser(UserCreateRequest userCreateRequest);
    UserResponse updateUser(Long id,UserUpdateRequest userUpateRequest);
    UserResponse updatePassword(Long id, PasswordUpdateRequest passwordUpdateRequest);
    LoginResponse login(LoginRequest request);
    User findByEmail(String email);
}
