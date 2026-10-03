package com.example.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(schema = "public", name = "products")
public class Product extends BaseEntity{

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String name;
    private String description;
    private BigDecimal price;
    private int stock;
    private String imageUrl;
    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

}
