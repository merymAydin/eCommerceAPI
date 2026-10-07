package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store,Long> {
    Store findByStoreName(String storeName);



}
