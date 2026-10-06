package com.example.ecommerce.controller;


import com.example.ecommerce.dto.request.CreateProductRequest;
import com.example.ecommerce.dto.request.UpdateProductRequest;
import com.example.ecommerce.dto.response.ProductResponse;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.service.interfaces.ProductService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.findAllProducts();
    }
    @GetMapping("/{id}")
    public ProductResponse getProductsById(@PathVariable Long id) {
        return productService.findProductById(id);
    }
    @GetMapping("/name")
    public ProductResponse getProductsByName(@RequestParam String productName ) {
        return productService.findProductByName(productName);
    }
    @PostMapping
    public ProductResponse createProduct(@Valid @RequestBody CreateProductRequest request) {
        return productService.createProduct(request);
    }
    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable Long id,@Valid @RequestBody UpdateProductRequest request) {
        return productService.updateProduct(id,request);
    }
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
         productService.deleteProductById(id);
    }

}
