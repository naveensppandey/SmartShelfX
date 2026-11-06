package com.smartshelfx.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DashboardStatsResponse
 * 
 * Aggregated metrics sent to the Angular Dashboard page.
 * Contains overview card figures, recent activity feeds, and chart datasets.
 */
public class DashboardStatsResponse {

    private long totalProducts;
    private BigDecimal totalInventoryValue;
    private long lowStockCount;
    private long outOfStockCount;
    private long totalCategories;
    private long pendingOrdersCount;
    private List<StockTransactionResponse> recentTransactions;
    private List<ProductResponse> lowStockProducts;
    private Map<String, Long> categoryProductCount;
    private List<TrendDataPoint> movementTrends;

    public DashboardStatsResponse() {
    }

    // Getters and Setters
    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public BigDecimal getTotalInventoryValue() {
        return totalInventoryValue;
    }

    public void setTotalInventoryValue(BigDecimal totalInventoryValue) {
        this.totalInventoryValue = totalInventoryValue;
    }

    public long getLowStockCount() {
        return lowStockCount;
    }

    public void setLowStockCount(long lowStockCount) {
        this.lowStockCount = lowStockCount;
    }

    public long getOutOfStockCount() {
        return outOfStockCount;
    }

    public void setOutOfStockCount(long outOfStockCount) {
        this.outOfStockCount = outOfStockCount;
    }

    public long getTotalCategories() {
        return totalCategories;
    }

    public void setTotalCategories(long totalCategories) {
        this.totalCategories = totalCategories;
    }

    public long getPendingOrdersCount() {
        return pendingOrdersCount;
    }

    public void setPendingOrdersCount(long pendingOrdersCount) {
        this.pendingOrdersCount = pendingOrdersCount;
    }

    public List<StockTransactionResponse> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<StockTransactionResponse> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }

    public List<ProductResponse> getLowStockProducts() {
        return lowStockProducts;
    }

    public void setLowStockProducts(List<ProductResponse> lowStockProducts) {
        this.lowStockProducts = lowStockProducts;
    }

    public Map<String, Long> getCategoryProductCount() {
        return categoryProductCount;
    }

    public void setCategoryProductCount(Map<String, Long> categoryProductCount) {
        this.categoryProductCount = categoryProductCount;
    }

    public List<TrendDataPoint> getMovementTrends() {
        return movementTrends;
    }

    public void setMovementTrends(List<TrendDataPoint> movementTrends) {
        this.movementTrends = movementTrends;
    }
}
