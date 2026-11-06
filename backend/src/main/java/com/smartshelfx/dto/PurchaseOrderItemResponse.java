package com.smartshelfx.dto;

import com.smartshelfx.entity.PurchaseOrderItem;
import java.math.BigDecimal;

/**
 * PurchaseOrderItemResponse
 * 
 * Response payload describing an item within a Purchase Order.
 */
public class PurchaseOrderItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private String unit;
    private Integer quantityOrdered;
    private BigDecimal unitCost;
    private BigDecimal totalCost;

    public PurchaseOrderItemResponse() {
    }

    public static PurchaseOrderItemResponse fromEntity(PurchaseOrderItem item) {
        PurchaseOrderItemResponse dto = new PurchaseOrderItemResponse();
        dto.setId(item.getId());
        dto.setQuantityOrdered(item.getQuantityOrdered());
        dto.setUnitCost(item.getUnitCost());
        dto.setTotalCost(item.getTotalCost());

        if (item.getProduct() != null) {
            dto.setProductId(item.getProduct().getId());
            dto.setProductName(item.getProduct().getName());
            dto.setProductSku(item.getProduct().getSku());
            dto.setUnit(item.getProduct().getUnit());
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

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Integer getQuantityOrdered() {
        return quantityOrdered;
    }

    public void setQuantityOrdered(Integer quantityOrdered) {
        this.quantityOrdered = quantityOrdered;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }
}
