package com.example.ecommerce.service;

import com.example.ecommerce.entity.Role;
import com.example.ecommerce.repository.RoleRepository;

import com.example.ecommerce.service.interfaces.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class RoleServiceImpl implements RoleService {
    private RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;

    }

    @Override
    public Role findById(Long id) {
        return roleRepository.findById(id).orElseThrow(()->new RuntimeException("Role not found"));
    }

    @Override
    public List<Role> findAll() {
        return roleRepository.findAll();
    }
}
