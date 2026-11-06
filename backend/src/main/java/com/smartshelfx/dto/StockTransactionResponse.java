package com.smartshelfx.dto;

import com.smartshelfx.entity.StockTransaction;
import java.time.LocalDateTime;

/**
 * StockTransactionResponse
 * 
 * Response payload describing a completed stock transaction.
 */
public class StockTransactionResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private String categoryName;
    private String transactionType;
    private Integer quantity;
    private Integer previousStock;
    private Integer newStock;
    private String notes;
    private Long userId;
    private String userName;
    private LocalDateTime transactionDate;

    public StockTransactionResponse() {
    }

    public static StockTransactionResponse fromEntity(StockTransaction tx) {
        StockTransactionResponse dto = new StockTransactionResponse();
        dto.setId(tx.getId());
        dto.setTransactionType(tx.getTransactionType());
        dto.setQuantity(tx.getQuantity());
        dto.setPreviousStock(tx.getPreviousStock());
        dto.setNewStock(tx.getNewStock());
        dto.setNotes(tx.getNotes());
        dto.setUserId(tx.getUserId());
        dto.setUserName(tx.getUserName());
        dto.setTransactionDate(tx.getTransactionDate());

        if (tx.getProduct() != null) {
            dto.setProductId(tx.getProduct().getId());
            dto.setProductName(tx.getProduct().getName());
            dto.setProductSku(tx.getProduct().getSku());
            if (tx.getProduct().getCategory() != null) {
                dto.setCategoryName(tx.getProduct().getCategory().getName());
            }
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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getPreviousStock() {
        return previousStock;
    }

    public void setPreviousStock(Integer previousStock) {
        this.previousStock = previousStock;
    }

    public Integer getNewStock() {
        return newStock;
    }

    public void setNewStock(Integer newStock) {
        this.newStock = newStock;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
}
