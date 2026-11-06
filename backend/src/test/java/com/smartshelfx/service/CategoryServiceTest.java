package com.smartshelfx.service;

import com.smartshelfx.dto.CategoryRequest;
import com.smartshelfx.dto.CategoryResponse;
import com.smartshelfx.entity.Category;
import com.smartshelfx.exception.BadRequestException;
import com.smartshelfx.exception.DuplicateResourceException;
import com.smartshelfx.repository.CategoryRepository;
import com.smartshelfx.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category sampleCategory;

    @BeforeEach
    void setUp() {
        sampleCategory = new Category("Perishables", "Fresh groceries and dairy");
        sampleCategory.setId(1L);
    }

    @Test
    @DisplayName("createCategory should throw DuplicateResourceException if category name already exists")
    void createCategory_DuplicateName() {
        when(categoryRepository.existsByNameIgnoreCase("Perishables")).thenReturn(true);

        CategoryRequest request = new CategoryRequest("Perishables", "Fresh groceries");

        assertThrows(DuplicateResourceException.class, () -> categoryService.createCategory(request));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("deleteCategory should fail if category still has linked products")
    void deleteCategory_HasProducts() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(sampleCategory));
        when(productRepository.countByCategoryId(1L)).thenReturn(5L);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> categoryService.deleteCategory(1L)
        );

        assertTrue(exception.getMessage().contains("still has 5 associated products"));
        verify(categoryRepository, never()).delete(any(Category.class));
    }
}
