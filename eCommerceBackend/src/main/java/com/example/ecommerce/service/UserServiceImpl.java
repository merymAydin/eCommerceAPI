package com.example.ecommerce.service;

import com.example.ecommerce.dto.request.UserCreateRequest;
import com.example.ecommerce.dto.request.UserUpateRequest;
import com.example.ecommerce.dto.response.UserResponse;
import com.example.ecommerce.entity.Role;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.RoleRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.interfaces.UserService;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserResponse createUser(UserCreateRequest userCreateRequest) {
        User user = new User();
        user.setUserName(userCreateRequest.username());
        user.setEmail(userCreateRequest.email());
        Role role = roleRepository.findById(userCreateRequest.roleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRole(role);
        userRepository.save(user);
        return new UserResponse(user.getUserName(), user.getEmail(), role.getRoleName());
    }

    @Override
    public UserResponse updateUser(Long id,UserUpateRequest userUpateRequest) {
        User user = userRepository.findById(id).orElseThrow(()-> new RuntimeException("User not found"));
        user.setUserName(userUpateRequest.username());
        user.setEmail(userUpateRequest.email());
        Role role = roleRepository.findById(userUpateRequest.roleId()).orElseThrow(()-> new RuntimeException("Role not found"));
        user.setRole(role);
        userRepository.save(user);
        return new UserResponse(user.getUserName(), user.getEmail(), role.getRoleName());
    }
}
