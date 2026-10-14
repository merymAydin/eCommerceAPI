package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product,Long> {
    Product findByName(String name);
    Optional<Product> findBySourceProductId(Long sourceProductId);
    Page<Product> findByCategory_Id(Long categoryId, Pageable pageable);
}

