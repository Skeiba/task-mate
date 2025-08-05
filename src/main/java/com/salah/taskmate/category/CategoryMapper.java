package com.salah.taskmate.category;

import com.salah.taskmate.category.dto.CategoryRequest;
import com.salah.taskmate.category.dto.CategoryResponse;
import com.salah.taskmate.user.User;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public Category toEntity(CategoryRequest request, User user) {
        return Category.builder()
                .name(request.getName())
                .user(user)
                .build();
    }

    public CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
