package com.example.ecommerce.service;

import com.example.ecommerce.entity.Role;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.RoleRepository;

import com.example.ecommerce.service.interfaces.RoleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@AllArgsConstructor
public class RoleServiceImpl implements RoleService {
    private RoleRepository roleRepository;


    @Override
    public Role findById(Long id) {
        return roleRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Role not found"));
    }

    @Override
    public Role findByName(String name) {
        return roleRepository.findByRoleName(name);
    }

    @Override
    public List<Role> findAll() {
        return roleRepository.findAll();
    }
}
