package com.library.service;

import com.library.dto.CategoryResponse;
import com.library.entity.Category;
import com.library.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    CategoryRepository categoryRepository;

    @InjectMocks
    CategoryService categoryService;

    @Test
    void shouldBuildCategoryTree() {
        Category lit = Category.builder().id(1L).name("文学").parentId(null).sortOrder(1).build();
        Category novel = Category.builder().id(2L).name("小说").parentId(1L).sortOrder(1).build();
        Category poetry = Category.builder().id(3L).name("诗词").parentId(1L).sortOrder(2).build();
        Category tech = Category.builder().id(4L).name("科技").parentId(null).sortOrder(2).build();

        when(categoryRepository.findAll()).thenReturn(List.of(lit, novel, poetry, tech));

        List<CategoryResponse> tree = categoryService.getCategoryTree();

        assertThat(tree).hasSize(2);
        assertThat(tree.get(0).getName()).isEqualTo("文学");
        assertThat(tree.get(0).getChildren()).hasSize(2);
        assertThat(tree.get(0).getChildren().get(0).getName()).isEqualTo("小说");
        assertThat(tree.get(1).getName()).isEqualTo("科技");
        assertThat(tree.get(1).getChildren()).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenNoCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of());
        List<CategoryResponse> tree = categoryService.getCategoryTree();
        assertThat(tree).isEmpty();
    }
}
