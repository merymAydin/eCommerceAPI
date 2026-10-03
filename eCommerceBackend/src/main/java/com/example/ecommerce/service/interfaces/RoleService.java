package com.example.ecommerce.service.interfaces;

import com.example.ecommerce.entity.Role;

import java.util.List;

public interface RoleService {
    Role findById(Long id);
    Role findByName(String name);
    List<Role> findAll();
}
