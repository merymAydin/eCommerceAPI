package com.example.ecommerce.service.interfaces;

import com.example.ecommerce.dto.request.CreateProductRequest;
import com.example.ecommerce.dto.request.UpdateProductRequest;
import com.example.ecommerce.dto.response.ProductResponse;


import java.util.List;

public interface ProductService {
    ProductResponse createProduct(CreateProductRequest createProductRequest);
    ProductResponse updateProduct(Long id,UpdateProductRequest updateProductRequest);
    List<ProductResponse> findAllProducts();
    ProductResponse findProductById(Long productId);
    ProductResponse findProductByName(String productName);
    void deleteProductById(Long productId);
}
