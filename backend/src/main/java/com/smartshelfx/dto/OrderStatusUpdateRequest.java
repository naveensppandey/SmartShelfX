package com.smartshelfx.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * OrderStatusUpdateRequest
 * 
 * Payload used to approve, reject, or mark a Purchase Order as received.
 */
public class OrderStatusUpdateRequest {

    @NotBlank(message = "Order status is required.")
    @Pattern(regexp = "^(APPROVED|REJECTED|RECEIVED)$", message = "Status must be 'APPROVED', 'REJECTED', or 'RECEIVED'.")
    private String status;

    private String notes;

    private Long userId;

    private String userName;

    public OrderStatusUpdateRequest() {
    }

    public OrderStatusUpdateRequest(String status, String notes) {
        this.status = status;
        this.notes = notes;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
