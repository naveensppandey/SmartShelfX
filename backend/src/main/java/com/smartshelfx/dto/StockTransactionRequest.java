package com.smartshelfx.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * StockTransactionRequest
 * 
 * Payload for recording an inventory addition (IN) or deduction (OUT).
 */
public class StockTransactionRequest {

    @NotNull(message = "Product ID is required.")
    private Long productId;

    @NotBlank(message = "Transaction type is required.")
    @Pattern(regexp = "^(IN|OUT)$", message = "Transaction type must be either 'IN' or 'OUT'.")
    private String transactionType;

    @NotNull(message = "Quantity is required.")
    @Min(value = 1, message = "Quantity must be at least 1 unit.")
    private Integer quantity;

    private String notes;

    private Long userId;

    private String userName;

    public StockTransactionRequest() {
    }

    public StockTransactionRequest(Long productId, String transactionType, Integer quantity, String notes) {
        this.productId = productId;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.notes = notes;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
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
}
