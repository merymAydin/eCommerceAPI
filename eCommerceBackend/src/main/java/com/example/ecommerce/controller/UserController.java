package com.example.ecommerce.controller;

import com.example.ecommerce.dto.request.LoginRequest;
import com.example.ecommerce.dto.request.PasswordUpdateRequest;
import com.example.ecommerce.dto.request.UserCreateRequest;
import com.example.ecommerce.dto.request.UserUpdateRequest;
import com.example.ecommerce.dto.response.LoginResponse;
import com.example.ecommerce.dto.response.UserResponse;
import com.example.ecommerce.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserResponse createUser(@Valid @RequestBody UserCreateRequest user) {
        return userService.createUser(user);
    }
    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id,@Valid @RequestBody UserUpdateRequest user) {
        return userService.updateUser(id,user);
    }
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }
    @PutMapping("/{id}/password")
    public UserResponse updatePassword(@PathVariable Long id,@Valid @RequestBody PasswordUpdateRequest passwordUpdateRequest) {
        return userService.updatePassword(id,passwordUpdateRequest);
    }
}
