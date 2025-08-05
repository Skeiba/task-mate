package com.salah.taskmate.category;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    @Override
    public List<Category> getCategoriesByIdsAndUserId(List<UUID> categoryIds, UUID userId) {
        return List.of();
    }
}
