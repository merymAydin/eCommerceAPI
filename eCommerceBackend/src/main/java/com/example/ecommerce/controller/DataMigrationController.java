package com.example.ecommerce.controller;

import com.example.ecommerce.dto.migration.ProductMigrationResponse;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.service.DataMigrationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/migration")
@AllArgsConstructor
public class DataMigrationController{
    private final DataMigrationService dataMigrationService;
    @PostMapping("/products/preview")
    public ResponseEntity<ProductMigrationResponse> previewProducts(@RequestParam int limit,@RequestParam int offset){
        return new ResponseEntity<>(dataMigrationService.fetchProducts(limit, offset), HttpStatus.OK);
    }
    @PostMapping("/categories")
    public ResponseEntity<List<Category>> migrateCategories() {
        return ResponseEntity.ok(dataMigrationService.migrateCategories());
    }
    @PostMapping("/products")
    public ResponseEntity<Integer> migrateProducts() {
        return ResponseEntity.ok(dataMigrationService.migrateProducts());
    }
}

