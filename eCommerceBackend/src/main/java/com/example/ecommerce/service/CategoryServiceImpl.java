package com.example.ecommerce.service;

import com.example.ecommerce.dto.request.CreateCategoryRequest;
import com.example.ecommerce.dto.request.UpdateCategoryRequest;
import com.example.ecommerce.dto.response.CategoryDetailResponse;
import com.example.ecommerce.dto.response.CategoryResponse;
import com.example.ecommerce.dto.response.ProductResponse;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.service.interfaces.CategoryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private CategoryRepository categoryRepository;

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getCode(),
                category.getTitle(),
                category.getImg(),
                category.getRating(),
                category.getGender()
        );
    }

    private CategoryDetailResponse toDetailResponse(Category category) {
        return new CategoryDetailResponse(
                category.getId(),
                category.getCode(),
                category.getTitle(),
                category.getImg(),
                category.getRating(),
                category.getGender(),
                category.getProducts()
                        .stream()
                        .map(p -> new ProductResponse(
                                p.getCategory().getId(),
                                p.getName(),
                                p.getDescription(),
                                p.getPrice(),
                                p.getStock(),
                                p.getImageUrl(),
                                p.getStore().getId()
                        ))
                        .toList()
        );
    }

    public List<CategoryResponse> findAllCategories() {
        return categoryRepository.findAll().stream()
                .map(category -> toResponse(category))
                .toList();
    }

    @Override
    public CategoryDetailResponse findCategoryById(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Category Not Found"));
        return toDetailResponse(category);
    }

    @Override
    public CategoryDetailResponse findCategoryByName(String name) {
        Category category=categoryRepository.findCategoryByTitle(name);
        if(category==null){
            throw new ResourceNotFoundException("Category Not Found");
        }
        return toDetailResponse(category);
    }

    @Override
    public CategoryResponse createCategory(CreateCategoryRequest req) {
        Category category=new Category();
        category.setCode(req.code());
        category.setTitle(req.title());
        category.setImg(req.img());
        category.setRating(req.rating());
        category.setGender(req.gender());
        Category savedCategory=categoryRepository.save(category);
        return toResponse(savedCategory);
    }

    @Override
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest req) {
        Category category = categoryRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Category Not Found"));
        category.setTitle(req.title());
        category.setImg(req.img());
        category.setRating(req.rating());
        category.setGender(req.gender());
        Category savedCategory=categoryRepository.save(category);
        return toResponse(savedCategory);
    }

    @Override
    public void deleteCategoryById(Long id) {
      categoryRepository.deleteById(id);
    }
}
