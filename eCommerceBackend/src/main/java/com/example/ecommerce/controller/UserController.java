package com.example.ecommerce.controller;

import com.example.ecommerce.dto.request.UserCreateRequest;
import com.example.ecommerce.dto.request.UserUpateRequest;
import com.example.ecommerce.dto.response.UserResponse;
import com.example.ecommerce.service.interfaces.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserResponse createUser(@RequestBody UserCreateRequest user) {
        return userService.createUser(user);
    }
    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id,@RequestBody UserUpateRequest user) {
        return userService.updateUser(id,user);
    }
}
