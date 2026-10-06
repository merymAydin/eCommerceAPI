package com.example.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(schema = "public", name = "categories")
public class Category extends BaseEntity{
    @OneToMany(mappedBy = "category")
    private List<Product> products;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String code;
    private String title;
    private String img;
    private BigDecimal rating;


}
