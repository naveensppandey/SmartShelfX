package com.smartshelfx.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * PurchaseOrderItemRequest
 * 
 * Individual line item submitted within a Purchase Order request.
 */
public class PurchaseOrderItemRequest {

    @NotNull(message = "Product ID is required for each order item.")
    private Long productId;

    @NotNull(message = "Quantity ordered is required.")
    @Min(value = 1, message = "Quantity ordered must be at least 1 unit.")
    private Integer quantityOrdered;

    @NotNull(message = "Unit cost is required.")
    @DecimalMin(value = "0.01", message = "Unit cost must be greater than zero.")
    private BigDecimal unitCost;

    public PurchaseOrderItemRequest() {
    }

    public PurchaseOrderItemRequest(Long productId, Integer quantityOrdered, BigDecimal unitCost) {
        this.productId = productId;
        this.quantityOrdered = quantityOrdered;
        this.unitCost = unitCost;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
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
}
