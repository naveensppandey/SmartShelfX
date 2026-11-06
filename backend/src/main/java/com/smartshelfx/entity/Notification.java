package com.smartshelfx.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Notification Entity
 * 
 * Maps to MySQL table 'notifications'.
 * Database-backed notification record alerting staff of inventory events.
 * 
 * Types:
 * - LOW_STOCK: Product stock has dipped below its minimum threshold.
 * - OUT_OF_STOCK: Product is depleted (zero stock).
 * - ORDER_UPDATE: Purchase order status transition (Approved, Received).
 * - SYSTEM: General inventory notices.
 */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false, length = 30)
    private String type; // "LOW_STOCK", "OUT_OF_STOCK", "ORDER_UPDATE", "SYSTEM"

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Notification() {
    }

    public Notification(String title, String message, String type, Long productId) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.productId = productId;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
