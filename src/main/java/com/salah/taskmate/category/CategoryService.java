package com.salah.taskmate.category;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    List<Category> getCategoriesByIdsAndUserId(List<UUID> categoryIds, UUID userId);
}
