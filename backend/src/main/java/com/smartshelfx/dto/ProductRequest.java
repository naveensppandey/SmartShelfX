package com.smartshelfx.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * ProductRequest
 * 
 * Payload received when creating or updating a Product in inventory.
 * Contains Bean Validation constraints to reject invalid input before reaching business logic.
 */
public class ProductRequest {

    @NotBlank(message = "Product name cannot be empty.")
    @Size(min = 2, max = 150, message = "Product name must be between 2 and 150 characters.")
    private String name;

    @NotBlank(message = "SKU (Stock Keeping Unit) is required.")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "SKU must contain only alphanumeric characters, dashes, and underscores.")
    @Size(min = 3, max = 50, message = "SKU must be between 3 and 50 characters.")
    private String sku;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters.")
    private String description;

    @NotNull(message = "Category ID is required.")
    private Long categoryId;

    private Long vendorId;

    private String vendorName;

    @NotNull(message = "Selling price is required.")
    @DecimalMin(value = "0.01", message = "Selling price must be greater than zero.")
    private BigDecimal price;

    @NotNull(message = "Cost price is required.")
    @DecimalMin(value = "0.01", message = "Cost price must be greater than zero.")
    private BigDecimal costPrice;

    @NotNull(message = "Current stock is required.")
    @Min(value = 0, message = "Current stock cannot be negative.")
    private Integer currentStock = 0;

    @NotNull(message = "Minimum stock threshold is required.")
    @Min(value = 0, message = "Minimum stock threshold cannot be negative.")
    private Integer minStockThreshold = 10;

    @NotNull(message = "Reorder quantity is required.")
    @Min(value = 1, message = "Reorder quantity must be at least 1 unit.")
    private Integer reorderQuantity = 50;

    private String unit = "pcs";

    public ProductRequest() {
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }

    public Integer getMinStockThreshold() {
        return minStockThreshold;
    }

    public void setMinStockThreshold(Integer minStockThreshold) {
        this.minStockThreshold = minStockThreshold;
    }

    public Integer getReorderQuantity() {
        return reorderQuantity;
    }

    public void setReorderQuantity(Integer reorderQuantity) {
        this.reorderQuantity = reorderQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
