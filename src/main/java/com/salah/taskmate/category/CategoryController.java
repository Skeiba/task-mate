package com.salah.taskmate.category;

import com.salah.taskmate.category.dto.CategoryRequest;
import com.salah.taskmate.category.dto.CategoryResponse;
import com.salah.taskmate.security.CustomUserDetails;
import com.salah.taskmate.shared.annotation.StandardApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @StandardApiResponse(message = "Category created successfully")
    public ResponseEntity<CategoryResponse> createCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CategoryRequest categoryRequest) {
        CategoryResponse response = categoryService.createCategory(userDetails.getId(), categoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @StandardApiResponse(message = "Categories retrieved successfully")
    public ResponseEntity<List<CategoryResponse>> getCategoriesByUserId(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<CategoryResponse> categories = categoryService.getCategoriesByUserId(userDetails.getId());
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{categoryId}")
    @StandardApiResponse(message = "Category retrieved successfully")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID categoryId) {
        CategoryResponse category = categoryService.getCategoryById(categoryId, userDetails.getId());
        return ResponseEntity.ok(category);
    }

    @PutMapping("/{categoryId}")
    @StandardApiResponse(message = "Category updated successfully")
    public ResponseEntity<CategoryResponse> updateCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID categoryId,
            @Valid @RequestBody CategoryRequest categoryRequest) {
        CategoryResponse response = categoryService.updateCategory(userDetails.getId(), categoryId, categoryRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{categoryId}")
    @StandardApiResponse(message = "Category deleted successfully")
    public ResponseEntity<Void> deleteCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID categoryId) {
        categoryService.deleteCategory(userDetails.getId(), categoryId);
        return ResponseEntity.noContent().build();
    }
}