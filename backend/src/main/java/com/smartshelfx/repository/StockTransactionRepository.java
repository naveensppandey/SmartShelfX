package com.smartshelfx.repository;

import com.smartshelfx.entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * StockTransactionRepository
 * 
 * Provides database access for stock movement audit records.
 */
@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

    /**
     * Retrieves all transactions for a product ordered by most recent first.
     */
    List<StockTransaction> findByProductIdOrderByTransactionDateDesc(Long productId);

    /**
     * Retrieves all transactions across the warehouse ordered by most recent first.
     */
    List<StockTransaction> findAllByOrderByTransactionDateDesc();

    /**
     * Returns top recent transactions (used for dashboard activity feed).
     */
    @Query("SELECT t FROM StockTransaction t ORDER BY t.transactionDate DESC LIMIT 10")
    List<StockTransaction> findRecentTransactions();
}
