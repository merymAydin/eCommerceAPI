package com.example.ecommerce.service;


import com.example.ecommerce.dto.migration.CategoryMigrationDto;

import com.example.ecommerce.dto.migration.ProductMigrationDto;
import com.example.ecommerce.dto.migration.ProductMigrationResponse;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Gender;
import com.example.ecommerce.entity.Product;

import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.StoreRepository;
import lombok.AllArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
@AllArgsConstructor
public class DataMigrationService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://workintech-fe-ecommerce.onrender.com")
            .build();


    public ProductMigrationResponse fetchProducts(int limit,int offset) {
        return restClient.get()
                .uri(UriBuilder -> UriBuilder
                        .path("/products")
                        .queryParam("limit",limit)
                        .queryParam("offset",offset)
                        .build()
                )
                .retrieve()
                .body(ProductMigrationResponse .class);

    }

    public Product migrateProduct(ProductMigrationDto productMigrationDto, Category category) {

        Product product = productRepository
                .findBySourceProductId(productMigrationDto.id())
                .orElseGet(Product::new);
        product.setSourceProductId(productMigrationDto.id());
        product.setName(productMigrationDto.name());
        product.setDescription(productMigrationDto.description());
        product.setPrice(productMigrationDto.price());
        product.setStock(productMigrationDto.stock());
        product.setCategory(category);
        product.setRating(productMigrationDto.rating());


            if (productMigrationDto.images() != null
                    && !productMigrationDto.images().isEmpty()) {
                product.setImageUrl(productMigrationDto.images().get(0).url());
            }

        System.out.println("SOURCE RATING: " + productMigrationDto.rating());


        return productRepository.save(product);
    }


    public int migrateProducts() {
        List<CategoryMigrationDto> sourceCategories = fetchCategories();

        Map<Long, Category> categoryMap = new HashMap<>();


        for (CategoryMigrationDto sourceCategory : sourceCategories) {
            Category category = categoryRepository.findByCode(sourceCategory.code());

            if (category == null) {
                throw new ResourceNotFoundException(
                        "Local category not found: " + sourceCategory.code()
                );
            }

            categoryMap.put(sourceCategory.id(), category);
        }

        ProductMigrationResponse response = fetchProducts(100, 0);
        int total = response.total();
        int offset = 0;
        int migratedCount = 0;

        while (total > offset) {
            if (offset > 0) {
                response = fetchProducts(100, offset);

            }
            for (ProductMigrationDto dto : response.products()) {
                Category category = categoryMap.get(dto.categoryId());
                migrateProduct(dto, category);
                migratedCount++;
            }
            offset +=100;
        }

        return migratedCount;

    }

    public List<CategoryMigrationDto> fetchCategories() {
        return restClient.get()
                .uri("/categories")
                .retrieve()
                .body(new ParameterizedTypeReference<List<CategoryMigrationDto>>() {});
    }


    public Category migrateCategory(CategoryMigrationDto categoryMigrationDto) {
        Category category = categoryRepository.findByCode(categoryMigrationDto.code());

        if (category == null) {
            category = new Category();
            category.setCode(categoryMigrationDto.code());
        }
        category.setTitle(categoryMigrationDto.title());
        category.setGender(
                "k".equals(categoryMigrationDto.gender())
                        ? Gender.FEMALE
                        : Gender.MALE
        );
        category.setRating(categoryMigrationDto.rating());
        category.setCode(categoryMigrationDto.code());
        category.setImg(categoryMigrationDto.img());
        return categoryRepository.save(category);
    }

    public List<Category> migrateCategories() {
        List<CategoryMigrationDto> categories = fetchCategories();
        List<Category> savedCategories = new ArrayList<>();

        for (CategoryMigrationDto dto : categories) {
            savedCategories.add(migrateCategory(dto));
        }

        return savedCategories;
    }


}
