package com.example.ecommerce.service.interfaces;

import com.example.ecommerce.dto.request.UserCreateRequest;
import com.example.ecommerce.dto.request.UserUpateRequest;
import com.example.ecommerce.dto.response.UserResponse;

public interface UserService {
    UserResponse createUser(UserCreateRequest userCreateRequest);
    UserResponse updateUser(Long id,UserUpateRequest userUpateRequest);
}
