package com.smartshelfx.service;

import com.smartshelfx.dto.DashboardStatsResponse;
import com.smartshelfx.dto.ProductResponse;
import com.smartshelfx.dto.StockTransactionResponse;
import com.smartshelfx.dto.TrendDataPoint;
import com.smartshelfx.entity.Category;
import com.smartshelfx.entity.Product;
import com.smartshelfx.entity.StockTransaction;
import com.smartshelfx.repository.CategoryRepository;
import com.smartshelfx.repository.ProductRepository;
import com.smartshelfx.repository.PurchaseOrderRepository;
import com.smartshelfx.repository.StockTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AnalyticsService
 * 
 * Aggregates live database statistics for executive dashboard views and inventory reporting.
 * Never invents mock figures: calculates actual balances from MySQL tables.
 */
@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockTransactionRepository transactionRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public AnalyticsService(ProductRepository productRepository,
                            CategoryRepository categoryRepository,
                            StockTransactionRepository transactionRepository,
                            PurchaseOrderRepository purchaseOrderRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    /**
     * Gathers complete metrics for the main system Dashboard.
     */
    public DashboardStatsResponse getDashboardStats() {
        List<Product> allProducts = productRepository.findAll();
        List<Category> allCategories = categoryRepository.findAll();
        List<StockTransaction> allTransactions = transactionRepository.findAllByOrderByTransactionDateDesc();

        DashboardStatsResponse stats = new DashboardStatsResponse();

        // 1. Total products count
        stats.setTotalProducts(allProducts.size());

        // 2. Total inventory value: sum(price * currentStock)
        BigDecimal totalValue = allProducts.stream()
                .filter(p -> p.getPrice() != null && p.getCurrentStock() != null && p.getCurrentStock() > 0)
                .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getCurrentStock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalInventoryValue(totalValue);

        // 3. Low stock and out-of-stock counts & items
        List<Product> lowStock = allProducts.stream().filter(Product::isLowStock).collect(Collectors.toList());
        List<Product> outOfStock = allProducts.stream().filter(Product::isOutOfStock).collect(Collectors.toList());

        stats.setLowStockCount(lowStock.size());
        stats.setOutOfStockCount(outOfStock.size());
        stats.setLowStockProducts(lowStock.stream().map(ProductResponse::fromEntity).collect(Collectors.toList()));

        // 4. Categories count & breakdown
        stats.setTotalCategories(allCategories.size());
        Map<String, Long> categoryMap = new HashMap<>();
        for (Category cat : allCategories) {
            long count = allProducts.stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(cat.getId()))
                    .count();
            categoryMap.put(cat.getName(), count);
        }
        stats.setCategoryProductCount(categoryMap);

        // 5. Pending purchase orders count
        stats.setPendingOrdersCount(purchaseOrderRepository.findByStatusOrderByCreatedAtDesc("PENDING").size());

        // 6. Recent 10 transactions
        stats.setRecentTransactions(allTransactions.stream()
                .limit(10)
                .map(StockTransactionResponse::fromEntity)
                .collect(Collectors.toList()));

        // 7. Movement trends aggregated by date for the last 14 days
        stats.setMovementTrends(calculateDailyMovementTrends(allTransactions));

        return stats;
    }

    /**
     * Calculates daily stock in/out sums for the last 14 days.
     */
    private List<TrendDataPoint> calculateDailyMovementTrends(List<StockTransaction> transactions) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Prepare map with past 14 days initialized to zero
        Map<String, TrendDataPoint> dailyMap = new LinkedHashMap<>();
        for (int i = 13; i >= 0; i--) {
            String dateStr = today.minusDays(i).format(formatter);
            dailyMap.put(dateStr, new TrendDataPoint(dateStr, 0, 0));
        }

        // Aggregate actual transaction records
        for (StockTransaction tx : transactions) {
            if (tx.getTransactionDate() != null) {
                String txDate = tx.getTransactionDate().toLocalDate().format(formatter);
                if (dailyMap.containsKey(txDate)) {
                    TrendDataPoint point = dailyMap.get(txDate);
                    if ("IN".equalsIgnoreCase(tx.getTransactionType())) {
                        point.setStockIn(point.getStockIn() + tx.getQuantity());
                    } else if ("OUT".equalsIgnoreCase(tx.getTransactionType())) {
                        point.setStockOut(point.getStockOut() + tx.getQuantity());
                    }
                }
            }
        }

        return new ArrayList<>(dailyMap.values());
    }
}
