package com.example.ecommerce.security.config;

import com.example.ecommerce.entity.Role;
import com.example.ecommerce.repository.RoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        if (roleRepository.findByRoleName("CUSTOMER") == null) {
            Role role = new Role();
            role.setRoleName("CUSTOMER");
            roleRepository.save(role);
        }

        if (roleRepository.findByRoleName("STORE") == null) {
            Role role = new Role();
            role.setRoleName("STORE");
            roleRepository.save(role);
        }

    }

}
