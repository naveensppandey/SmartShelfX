package com.smartshelfx.dto;

import com.smartshelfx.entity.PurchaseOrder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PurchaseOrderResponse
 * 
 * Response payload describing a Purchase Order and its child line items.
 */
public class PurchaseOrderResponse {

    private Long id;
    private String orderNumber;
    private Long vendorId;
    private String vendorName;
    private String status;
    private BigDecimal totalAmount;
    private String notes;
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime receivedAt;
    private List<PurchaseOrderItemResponse> items;

    public PurchaseOrderResponse() {
    }

    public static PurchaseOrderResponse fromEntity(PurchaseOrder po) {
        PurchaseOrderResponse dto = new PurchaseOrderResponse();
        dto.setId(po.getId());
        dto.setOrderNumber(po.getOrderNumber());
        dto.setVendorId(po.getVendorId());
        dto.setVendorName(po.getVendorName());
        dto.setStatus(po.getStatus());
        dto.setTotalAmount(po.getTotalAmount());
        dto.setNotes(po.getNotes());
        dto.setCreatedBy(po.getCreatedBy());
        dto.setCreatedByName(po.getCreatedByName());
        dto.setCreatedAt(po.getCreatedAt());
        dto.setUpdatedAt(po.getUpdatedAt());
        dto.setReceivedAt(po.getReceivedAt());

        if (po.getItems() != null) {
            dto.setItems(po.getItems().stream()
                    .map(PurchaseOrderItemResponse::fromEntity)
                    .collect(Collectors.toList()));
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

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
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

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public List<PurchaseOrderItemResponse> getItems() {
        return items;
    }

    public void setItems(List<PurchaseOrderItemResponse> items) {
        this.items = items;
    }
}
