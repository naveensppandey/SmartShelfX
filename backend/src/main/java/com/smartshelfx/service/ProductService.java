package com.smartshelfx.service;

import com.smartshelfx.dto.ProductRequest;
import com.smartshelfx.dto.ProductResponse;
import com.smartshelfx.entity.Category;
import com.smartshelfx.entity.Product;
import com.smartshelfx.exception.BadRequestException;
import com.smartshelfx.exception.DuplicateResourceException;
import com.smartshelfx.exception.ResourceNotFoundException;
import com.smartshelfx.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ProductService
 * 
 * Core business service managing Product inventory.
 * Enforces business rules:
 * 1. SKU uniqueness across all products.
 * 2. Current stock must never be negative.
 * 3. Products must be associated with a valid Category.
 */
@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    /**
     * Retrieves all products formatted as Response DTOs.
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single product by its database ID.
     */
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = findProductEntityById(id);
        return ProductResponse.fromEntity(product);
    }

    /**
     * Finds and returns the raw JPA Product entity.
     */
    @Transactional(readOnly = true)
    public Product findProductEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    /**
     * Retrieves a product by its unique SKU code.
     */
    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySkuIgnoreCase(sku.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "SKU", sku));
        return ProductResponse.fromEntity(product);
    }

    /**
     * Searches and filters products by optional search keyword and category ID.
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String keyword, Long categoryId, String stockStatus) {
        List<Product> products;

        if (keyword != null || categoryId != null) {
            String trimmedKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
            products = productRepository.searchProducts(trimmedKeyword, categoryId);
        } else {
            products = productRepository.findAll();
        }

        // Apply in-memory filter if stockStatus parameter is specified
        if ("LOW_STOCK".equalsIgnoreCase(stockStatus)) {
            products = products.stream().filter(Product::isLowStock).collect(Collectors.toList());
        } else if ("OUT_OF_STOCK".equalsIgnoreCase(stockStatus)) {
            products = products.stream().filter(Product::isOutOfStock).collect(Collectors.toList());
        }

        return products.stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Returns all products that have low stock (<= threshold and > 0).
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockProducts() {
        return productRepository.findLowStockProducts().stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Returns all products that are completely out of stock (<= 0).
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getOutOfStockProducts() {
        return productRepository.findOutOfStockProducts().stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new Product.
     * Validates SKU uniqueness and category presence.
     */
    public ProductResponse createProduct(ProductRequest request) {
        String trimmedSku = request.getSku().trim().toUpperCase();

        if (productRepository.existsBySkuIgnoreCase(trimmedSku)) {
            throw new DuplicateResourceException("A product with SKU '" + trimmedSku + "' already exists.");
        }

        Category category = categoryService.findCategoryEntityById(request.getCategoryId());

        Product product = new Product();
        copyRequestToEntity(request, product, category, trimmedSku);

        Product savedProduct = productRepository.save(product);
        return ProductResponse.fromEntity(savedProduct);
    }

    /**
     * Updates an existing product.
     * Re-verifies SKU uniqueness if the SKU is being altered.
     */
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProductEntityById(id);
        String trimmedSku = request.getSku().trim().toUpperCase();

        // If SKU changed, verify that the new SKU is not claimed by another product
        if (!product.getSku().equalsIgnoreCase(trimmedSku) && productRepository.existsBySkuIgnoreCase(trimmedSku)) {
            throw new DuplicateResourceException("Another product with SKU '" + trimmedSku + "' already exists.");
        }

        Category category = categoryService.findCategoryEntityById(request.getCategoryId());
        copyRequestToEntity(request, product, category, trimmedSku);

        Product updatedProduct = productRepository.save(product);
        return ProductResponse.fromEntity(updatedProduct);
    }

    /**
     * Deletes a product by ID.
     */
    public void deleteProduct(Long id) {
        Product product = findProductEntityById(id);
        productRepository.delete(product);
    }

    /**
     * Updates product stock directly.
     * Prevents negative stock.
     */
    public Product updateStock(Long productId, int quantityChange) {
        Product product = findProductEntityById(productId);
        int newStock = product.getCurrentStock() + quantityChange;

        if (newStock < 0) {
            throw new BadRequestException("Cannot deduct " + Math.abs(quantityChange) +
                    " units. Current stock is only " + product.getCurrentStock() + ".");
        }

        product.setCurrentStock(newStock);
        return productRepository.save(product);
    }

    /**
     * Helper to populate entity fields from a validated request DTO.
     */
    private void copyRequestToEntity(ProductRequest request, Product product, Category category, String normalizedSku) {
        product.setName(request.getName().trim());
        product.setSku(normalizedSku);
        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setVendorId(request.getVendorId());
        product.setVendorName(request.getVendorName());
        product.setPrice(request.getPrice());
        product.setCostPrice(request.getCostPrice());
        product.setCurrentStock(request.getCurrentStock());
        product.setMinStockThreshold(request.getMinStockThreshold());
        product.setReorderQuantity(request.getReorderQuantity());
        product.setUnit(request.getUnit() != null ? request.getUnit().trim() : "pcs");
    }
}
