package com.example.ecommerce.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(schema = "public", name = "users")

public class User extends BaseEntity {

    private String userName;

    @Column(unique = true, nullable = false)
    private String email;


    private String password;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    private String stripeCustomerId;
}