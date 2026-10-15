package com.example.ecommerce.service;

import com.example.ecommerce.dto.request.CreateProductRequest;
import com.example.ecommerce.dto.request.UpdateProductRequest;
import com.example.ecommerce.dto.response.ProductPageResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final StoreRepository storeRepository;
    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;

    private ProductResponse toResponse(Product product) {
        if (product.getStore() == null) {
            throw new ResourceNotFoundException("Store not found for product id: " + product.getId());
        }
        return new ProductResponse(
                product.getId(),
                product.getCategory().getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getImageUrl(),
                product.getStore().getId(),
                product.getRating(),
                product.getReviewCount()
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
    public ProductPageResponse findAllProducts(Pageable pageable,Long categoryId) {
        Page<Product> productPage;

        if (categoryId == null) {
            productPage = productRepository.findAll(pageable);
        }else {
            productPage = productRepository.findByCategory_Id(categoryId, pageable);
        }
        List<ProductResponse> products = productPage.getContent()
                .stream()
                .map(product -> toResponse(product))
                .toList();

        return new ProductPageResponse(products, productPage.getTotalElements());

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
