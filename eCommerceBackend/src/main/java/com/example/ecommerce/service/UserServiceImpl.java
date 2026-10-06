package com.example.ecommerce.service;

import com.example.ecommerce.dto.request.LoginRequest;
import com.example.ecommerce.dto.request.PasswordUpdateRequest;
import com.example.ecommerce.dto.request.UserCreateRequest;
import com.example.ecommerce.dto.request.UserUpdateRequest;
import com.example.ecommerce.dto.response.LoginResponse;
import com.example.ecommerce.dto.response.UserResponse;
import com.example.ecommerce.entity.Role;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.security.service.JwtService;
import com.example.ecommerce.service.interfaces.RoleService;
import com.example.ecommerce.service.interfaces.UserService;
import lombok.AllArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getUserName(), user.getEmail(),user.getRole().getRoleName());
    }


    @Override
    public UserResponse createUser(UserCreateRequest userCreateRequest) {
        User user = new User();
        user.setUserName(userCreateRequest.username());
        user.setEmail(userCreateRequest.email());
        Role role = roleService.findById(userCreateRequest.roleId());
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(userCreateRequest.password()));
        User savedUser = userRepository.save(user);
        return toUserResponse(savedUser);
    }

    @Override
    public UserResponse updateUser(Long id,UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("User not found"));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User authenticatedUser = (User) authentication.getPrincipal();
        Long authenticatedUserId = authenticatedUser.getId();
        if (!user.getId().equals(authenticatedUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        user.setUserName(userUpdateRequest.username());
        user.setEmail(userUpdateRequest.email());
        User savedUser = userRepository.save(user);
        return toUserResponse(savedUser);

    }
    @Override
    public UserResponse updatePassword(Long id, PasswordUpdateRequest passwordUpdateRequest) {
        User user = userRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("User not found"));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User authenticatedUser = (User) authentication.getPrincipal();



        if (!user.getId().equals(authenticatedUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (!passwordEncoder.matches(passwordUpdateRequest.currentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }


        user.setPassword(passwordEncoder.encode(passwordUpdateRequest.newPassword()));
        User savedUser = userRepository.save(user);

        return toUserResponse(savedUser);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
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
        String token = jwtService.generateToken(user);
        UserResponse userResponse = new UserResponse(
                user.getUserName(),
                user.getEmail(),
                user.getRole().getRoleName()
        );


        return new LoginResponse(token,userResponse);

    }

    @Override
    public User findByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }
        return user;
    }
}
