package com.salah.taskmate.category;

import com.salah.taskmate.category.dto.CategoryRequest;
import com.salah.taskmate.category.dto.CategoryResponse;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    CategoryResponse createCategory(UUID userId, CategoryRequest categoryRequest);
    List<CategoryResponse> getCategoriesByUserId(UUID userId);
    CategoryResponse getCategoryById(UUID userId, UUID categoryId);
    CategoryResponse updateCategory(UUID userId, UUID categoryId, CategoryRequest categoryRequest);
    void deleteCategory(UUID userId, UUID categoryId);
    List<Category> getCategoriesByIdsAndUserId(List<UUID> categoryIds, UUID userId);
}
