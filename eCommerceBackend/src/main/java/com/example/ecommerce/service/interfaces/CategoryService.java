package com.example.ecommerce.service.interfaces;

import com.example.ecommerce.entity.Category;

public interface CategoryService {
    Category findCategoryById(Long id);
    Category findCategoryByName(String name);
}
