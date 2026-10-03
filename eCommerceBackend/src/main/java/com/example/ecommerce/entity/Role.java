package com.example.ecommerce.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(schema = "public", name = "roles")
public class Role extends BaseEntity{
    private String roleName;
    private String store;
    private String customer;

}
