package com.smartshelfx.service;

import com.smartshelfx.dto.CategoryRequest;
import com.smartshelfx.dto.CategoryResponse;
import com.smartshelfx.entity.Category;
import com.smartshelfx.exception.BadRequestException;
import com.smartshelfx.exception.DuplicateResourceException;
import com.smartshelfx.exception.ResourceNotFoundException;
import com.smartshelfx.repository.CategoryRepository;
import com.smartshelfx.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CategoryService
 * 
 * Handles business rules for Category management.
 * Enforces category name uniqueness and prevents deleting categories with associated products.
 */
@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    /**
     * Retrieves all categories along with their current product count.
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single category by ID.
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = findCategoryEntityById(id);
        return mapToResponse(category);
    }

    /**
     * Finds and returns the raw Category JPA entity.
     */
    @Transactional(readOnly = true)
    public Category findCategoryEntityById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    /**
     * Creates a new Category.
     * Prevents duplicate category names.
     */
    public CategoryResponse createCategory(CategoryRequest request) {
        String trimmedName = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new DuplicateResourceException("A category with name '" + trimmedName + "' already exists.");
        }

        Category category = new Category();
        category.setName(trimmedName);
        category.setDescription(request.getDescription());

        Category savedCategory = categoryRepository.save(category);
        return mapToResponse(savedCategory);
    }

    /**
     * Updates an existing Category.
     */
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = findCategoryEntityById(id);
        String trimmedName = request.getName().trim();

        // Check if name is being changed and if new name already exists elsewhere
        if (!category.getName().equalsIgnoreCase(trimmedName) && categoryRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new DuplicateResourceException("Another category with name '" + trimmedName + "' already exists.");
        }

        category.setName(trimmedName);
        category.setDescription(request.getDescription());

        Category updated = categoryRepository.save(category);
        return mapToResponse(updated);
    }

    /**
     * Deletes a Category by ID.
     * Business rule: Cannot delete if products are currently assigned to this category.
     */
    public void deleteCategory(Long id) {
        Category category = findCategoryEntityById(id);

        long productCount = productRepository.countByCategoryId(id);
        if (productCount > 0) {
            throw new BadRequestException("Cannot delete category '" + category.getName() + "' because it still has " +
                    productCount + " associated products. Reassign or delete products first.");
        }

        categoryRepository.delete(category);
    }

    /**
     * Helper to map Entity -> Response DTO including product count.
     */
    private CategoryResponse mapToResponse(Category category) {
        long count = productRepository.countByCategoryId(category.getId());
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getCreatedAt(),
                count
        );
    }
}
