package com.smartshelfx.controller;

import com.smartshelfx.dto.ProductRequest;
import com.smartshelfx.dto.ProductResponse;
import com.smartshelfx.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ProductController
 * 
 * Exposes REST API endpoints for Product CRUD operations,
 * search filtering, and low/out-of-stock monitoring.
 * Base Path: /api/products
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * GET /api/products
     * Returns all products, optionally filtered by keyword, category, or stock status.
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String stockStatus) {
        return ResponseEntity.ok(productService.searchProducts(keyword, categoryId, stockStatus));
    }

    /**
     * GET /api/products/{id}
     * Returns a single product by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * GET /api/products/sku/{sku}
     * Returns a product by its unique SKU code.
     */
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponse> getProductBySku(@PathVariable String sku) {
        return ResponseEntity.ok(productService.getProductBySku(sku));
    }

    /**
     * GET /api/products/low-stock
     * Returns all products that have reached or dropped below their minimum stock threshold.
     */
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductResponse>> getLowStockProducts() {
        return ResponseEntity.ok(productService.getLowStockProducts());
    }

    /**
     * GET /api/products/out-of-stock
     * Returns all products with zero stock.
     */
    @GetMapping("/out-of-stock")
    public ResponseEntity<List<ProductResponse>> getOutOfStockProducts() {
        return ResponseEntity.ok(productService.getOutOfStockProducts());
    }

    /**
     * POST /api/products
     * Creates a new product in the catalog.
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse created = productService.createProduct(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * PUT /api/products/{id}
     * Updates an existing product.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    /**
     * DELETE /api/products/{id}
     * Deletes a product from inventory.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
