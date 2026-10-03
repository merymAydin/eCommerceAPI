package com.example.ecommerce.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(schema = "public", name = "users")

public class User extends BaseEntity {

    private String userName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    private String stripeCustomerId;
    @Column(name = "created_at")


    private Instant createdAt;
    @Column(name = "created_by")


    private Integer createdBy;
    @Column(name = "is_deleted")


    private Boolean isDeleted;
    @Column(name = "status")


    private Integer status;
    @Column(name = "updated_at")


    private Instant updatedAt;
    @Column(name = "updated_by")

    private Integer updatedBy;


    @Size(max = 255)
    @Column(name = "banner")
    private String banner;


    @Size(max = 255)
    @Column(name = "bio")
    private String bio;


    @Column(name = "birthday")
    private Instant birthday;


    @Column(name = "lan")
    private Float lan;


    @Column(name = "lon")
    private Float lon;


    @Size(max = 255)
    @Column(name = "photo")
    private String photo;
}