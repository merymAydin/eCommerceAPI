package com.example.ecommerce.controller;


import com.example.ecommerce.entity.Role;
import com.example.ecommerce.service.interfaces.RoleService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/roles")
@AllArgsConstructor
public class RoleController {
    private RoleService roleService;

    @GetMapping
    public List<Role> getAll(){
        return roleService.findAll();
    }
    @GetMapping("/{id}")
    public Role getById(@PathVariable Long id){
        return roleService.findById(id);
    }
}
