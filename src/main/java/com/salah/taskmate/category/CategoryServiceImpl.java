package com.salah.taskmate.category;

import com.salah.taskmate.category.dto.CategoryRequest;
import com.salah.taskmate.category.dto.CategoryResponse;
import com.salah.taskmate.user.User;
import com.salah.taskmate.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper  categoryMapper;
    private final UserService  userService;

    private static final String CATEGORY_NOT_FOUND = "Category not found";

    @Override
    public CategoryResponse createCategory(UUID userId, CategoryRequest categoryRequest) {
        User user = userService.findById(userId);

        Category category = categoryMapper.toEntity(categoryRequest, user);

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    public List<CategoryResponse> getCategoriesByUserId(UUID userId) {
        return categoryRepository.findAllByUserId(userId).stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    public CategoryResponse getCategoryById(UUID categoryId, UUID userId) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new EntityNotFoundException(CATEGORY_NOT_FOUND));

        return categoryMapper.toResponse(category);
    }


    @Override
    public CategoryResponse updateCategory(UUID userId, UUID categoryId, CategoryRequest categoryRequest) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new EntityNotFoundException(CATEGORY_NOT_FOUND));

        category.setName(categoryRequest.getName());
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(UUID userId, UUID categoryId) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new EntityNotFoundException(CATEGORY_NOT_FOUND));
        categoryRepository.delete(category);
    }

    @Override
    public List<Category> getCategoriesByIdsAndUserId(List<UUID> categoryIds, UUID userId) {
        List<Category> categories = categoryRepository.findAllById(categoryIds);

        boolean allBelongsToUser = categories.stream()
                .allMatch(category -> category.getUser().getId().equals(userId));

        if (!allBelongsToUser) {
            throw new AccessDeniedException("One or more categories do not belong to this user");
        }
        return categories;
    }
}
