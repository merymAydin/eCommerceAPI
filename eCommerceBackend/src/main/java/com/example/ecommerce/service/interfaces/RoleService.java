package com.example.ecommerce.service.interfaces;

import com.example.ecommerce.entity.Role;

import java.util.List;

public interface RoleService {
    Role findById(Long id);
    List<Role> findAll();
}
