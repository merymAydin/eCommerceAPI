package com.example.ecommerce.service.interfaces;

import com.example.ecommerce.dto.request.CreateCategoryRequest;
import com.example.ecommerce.dto.request.UpdateCategoryRequest;
import com.example.ecommerce.dto.response.CategoryDetailResponse;
import com.example.ecommerce.dto.response.CategoryResponse;
import com.example.ecommerce.entity.Category;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> findAllCategories();

    CategoryDetailResponse findCategoryById(Long id);

    CategoryDetailResponse findCategoryByName(String name);

    CategoryResponse createCategory(CreateCategoryRequest req);

    CategoryResponse updateCategory(Long id, UpdateCategoryRequest req);

    void deleteCategoryById(Long id);
}
