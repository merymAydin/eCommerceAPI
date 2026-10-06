package com.example.ecommerce.service;

import com.example.ecommerce.dto.request.CreateProductRequest;
import com.example.ecommerce.dto.request.UpdateProductRequest;
import com.example.ecommerce.dto.response.ProductResponse;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.Store;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.StoreRepository;
import com.example.ecommerce.service.interfaces.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final StoreRepository storeRepository;
    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getCategory().getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getImageUrl(),
                product.getStore().getId()

        );
    }


    @Override
    public ProductResponse createProduct(CreateProductRequest createProductRequest) {
        Product product = new Product();

        Category category =categoryRepository.findById(createProductRequest.categoryId()).orElseThrow(()->new ResourceNotFoundException("Category not found"));
        product.setCategory(category);
        product.setName(createProductRequest.name());
        product.setDescription(createProductRequest.description());
        product.setPrice(createProductRequest.price());
        product.setStock(createProductRequest.stock());
        product.setImageUrl(createProductRequest.imageUrl());

        Store store = storeRepository.findById(createProductRequest.storeId()).orElseThrow(()->new ResourceNotFoundException("Store not found"));

        product.setStore(store);
        Product savedProduct = productRepository.save(product);

        return toResponse(savedProduct);
    }

    @Override
    public ProductResponse updateProduct(Long id,UpdateProductRequest updateProductRequest) {
        Product product = productRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Product not found"));
        product.setName(updateProductRequest.name());
        product.setDescription(updateProductRequest.description());
        product.setPrice(updateProductRequest.price());
        product.setStock(updateProductRequest.stock());
        product.setImageUrl(updateProductRequest.imageUrl());

        Category category = categoryRepository.findById(updateProductRequest.categoryId()).orElseThrow(()->new ResourceNotFoundException("Category not found"));
        product.setCategory(category);
        Product savedProduct = productRepository.save(product);

        return toResponse(savedProduct);
    }

    @Override
    public List<ProductResponse> findAllProducts() {
        return productRepository.findAll().stream().map(p->toResponse(p)).toList();
    }

    @Override
    public ProductResponse findProductById(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(()->new ResourceNotFoundException("Product not found"));
        return toResponse(product);
    }

    @Override
    public ProductResponse findProductByName(String productName) {
        Product product = productRepository.findByName(productName);

        if (product == null) {
            throw new ResourceNotFoundException("Product not found");
        }

        return toResponse(product);
    }

    @Override
    public void deleteProductById(Long productId) {
        productRepository.deleteById(productId);
    }
}
