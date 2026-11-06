package com.smartshelfx.dto;

import com.smartshelfx.entity.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ProductResponse
 * 
 * Response schema sent to the frontend displaying product details,
 * calculated stock status, and total inventory value.
 */
public class ProductResponse {

    private Long id;
    private String name;
    private String sku;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Long vendorId;
    private String vendorName;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Integer currentStock;
    private Integer minStockThreshold;
    private Integer reorderQuantity;
    private String unit;
    private String stockStatus; // "IN_STOCK", "LOW_STOCK", "OUT_OF_STOCK"
    private BigDecimal totalInventoryValue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProductResponse() {
    }

    /**
     * Convenient mapper method converting a Product entity into a ProductResponse DTO.
     */
    public static ProductResponse fromEntity(Product product) {
        ProductResponse dto = new ProductResponse();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSku(product.getSku());
        dto.setDescription(product.getDescription());
        
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        
        dto.setVendorId(product.getVendorId());
        dto.setVendorName(product.getVendorName());
        dto.setPrice(product.getPrice());
        dto.setCostPrice(product.getCostPrice());
        dto.setCurrentStock(product.getCurrentStock());
        dto.setMinStockThreshold(product.getMinStockThreshold());
        dto.setReorderQuantity(product.getReorderQuantity());
        dto.setUnit(product.getUnit());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        // Derive stock status
        if (product.getCurrentStock() <= 0) {
            dto.setStockStatus("OUT_OF_STOCK");
        } else if (product.getCurrentStock() <= product.getMinStockThreshold()) {
            dto.setStockStatus("LOW_STOCK");
        } else {
            dto.setStockStatus("IN_STOCK");
        }

        // Calculate total inventory value: currentStock * price
        if (product.getPrice() != null && product.getCurrentStock() != null) {
            dto.setTotalInventoryValue(product.getPrice().multiply(BigDecimal.valueOf(product.getCurrentStock())));
        } else {
            dto.setTotalInventoryValue(BigDecimal.ZERO);
        }

        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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

    public String getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(String stockStatus) {
        this.stockStatus = stockStatus;
    }

    public BigDecimal getTotalInventoryValue() {
        return totalInventoryValue;
    }

    public void setTotalInventoryValue(BigDecimal totalInventoryValue) {
        this.totalInventoryValue = totalInventoryValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
