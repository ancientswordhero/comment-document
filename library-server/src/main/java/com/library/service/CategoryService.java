package com.library.service;

import com.library.dto.CategoryResponse;
import com.library.entity.Category;
import com.library.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> getCategoryTree() {
        List<Category> all = categoryRepository.findAll();
        
        // 按名称去重，保留第一个出现的分类
        Map<String, Category> uniqueByName = new LinkedHashMap<>();
        for (Category cat : all) {
            uniqueByName.putIfAbsent(cat.getName(), cat);
        }
        List<Category> uniqueCategories = new ArrayList<>(uniqueByName.values());
        
        Map<Long, List<Category>> childrenMap = uniqueCategories.stream()
            .filter(c -> c.getParentId() != null)
            .collect(Collectors.groupingBy(Category::getParentId));

        return uniqueCategories.stream()
            .filter(c -> c.getParentId() == null)
            .sorted(Comparator.comparingInt(Category::getSortOrder))
            .map(c -> buildResponse(c, childrenMap))
            .collect(Collectors.toList());
    }

    private CategoryResponse buildResponse(Category category, Map<Long, List<Category>> childrenMap) {
        List<CategoryResponse> children = childrenMap
            .getOrDefault(category.getId(), List.of())
            .stream()
            .sorted(Comparator.comparingInt(Category::getSortOrder))
            .map(c -> buildResponse(c, childrenMap))
            .collect(Collectors.toList());

        return CategoryResponse.builder()
            .id(category.getId())
            .name(category.getName())
            .sortOrder(category.getSortOrder())
            .children(children)
            .build();
    }
}
