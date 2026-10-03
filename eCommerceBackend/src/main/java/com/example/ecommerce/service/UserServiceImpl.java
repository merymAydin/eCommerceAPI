package com.example.ecommerce.service;

import com.example.ecommerce.dto.request.LoginRequest;
import com.example.ecommerce.dto.request.UserCreateRequest;
import com.example.ecommerce.dto.request.UserUpdateRequest;
import com.example.ecommerce.dto.response.UserResponse;
import com.example.ecommerce.entity.Role;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.interfaces.RoleService;
import com.example.ecommerce.service.interfaces.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ResponseStatusException;


@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;


    @Override
    public UserResponse createUser(UserCreateRequest userCreateRequest) {
        User user = new User();
        user.setUserName(userCreateRequest.username());
        user.setEmail(userCreateRequest.email());
        Role role = roleService.findById(userCreateRequest.roleId());
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(userCreateRequest.password()));
        User savedUser = userRepository.save(user);
        return new UserResponse(savedUser.getUserName(), savedUser.getEmail(), savedUser.getRole().getRoleName());
    }

    @Override
    public UserResponse updateUser(Long id,UserUpdateRequest userUpateRequest) {
        User user = userRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("User not found"));
        user.setUserName(userUpateRequest.username());
        user.setEmail(userUpateRequest.email());
        Role role = roleService.findById(userUpateRequest.roleId());
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(userUpateRequest.password()));
        User savedUser = userRepository.save(user);
        return new UserResponse(savedUser.getUserName(), savedUser.getEmail(),savedUser.getRole().getRoleName());
    }

    @Override
    public UserResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email());

        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        return new UserResponse(user.getUserName(), user.getEmail(), user.getRole().getRoleName());

    }
}
