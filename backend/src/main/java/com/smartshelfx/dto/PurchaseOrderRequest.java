package com.smartshelfx.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * PurchaseOrderRequest
 * 
 * Payload received when creating a new Purchase Order.
 */
public class PurchaseOrderRequest {

    private Long vendorId;

    private String vendorName;

    @Size(max = 500, message = "Notes cannot exceed 500 characters.")
    private String notes;

    private Long createdBy;

    private String createdByName;

    @NotEmpty(message = "Purchase order must contain at least one line item.")
    @Valid
    private List<PurchaseOrderItemRequest> items;

    public PurchaseOrderRequest() {
    }

    // Getters and Setters
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public List<PurchaseOrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<PurchaseOrderItemRequest> items) {
        this.items = items;
    }
}
