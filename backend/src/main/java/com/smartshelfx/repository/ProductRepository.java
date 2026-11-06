package com.smartshelfx.repository;

import com.smartshelfx.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ProductRepository
 * 
 * Spring Data JPA repository for Product entities.
 * Includes query methods for inventory tracking, SKU verification, and search filtering.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Checks if a SKU already exists in the system (case-insensitive).
     */
    boolean existsBySkuIgnoreCase(String sku);

    /**
     * Finds a single product by its unique SKU code.
     */
    Optional<Product> findBySkuIgnoreCase(String sku);

    /**
     * Finds all products belonging to a specific category.
     */
    List<Product> findByCategoryId(Long categoryId);

    /**
     * Counts how many products belong to a category (used before category deletion).
     */
    long countByCategoryId(Long categoryId);

    /**
     * Finds products where current stock is less than or equal to minimum threshold
     * and strictly greater than zero (Low Stock).
     */
    @Query("SELECT p FROM Product p WHERE p.currentStock <= p.minStockThreshold AND p.currentStock > 0")
    List<Product> findLowStockProducts();

    /**
     * Finds products where current stock is zero or less (Out of Stock).
     */
    @Query("SELECT p FROM Product p WHERE p.currentStock <= 0")
    List<Product> findOutOfStockProducts();

    /**
     * Finds products assigned to a specific vendor ID.
     */
    List<Product> findByVendorId(Long vendorId);

    /**
     * Flexible search filter: searches across product name, SKU, or category name.
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId)")
    List<Product> searchProducts(@Param("keyword") String keyword, @Param("categoryId") Long categoryId);
}
