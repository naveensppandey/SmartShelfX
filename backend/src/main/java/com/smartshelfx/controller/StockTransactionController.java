package com.smartshelfx.controller;

import com.smartshelfx.dto.StockTransactionRequest;
import com.smartshelfx.dto.StockTransactionResponse;
import com.smartshelfx.service.StockTransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * StockTransactionController
 * 
 * REST API endpoints for executing stock adjustments (IN/OUT)
 * and reviewing audit trails.
 * Base Path: /api/transactions
 */
@RestController
@RequestMapping("/api/transactions")
public class StockTransactionController {

    private final StockTransactionService transactionService;

    public StockTransactionController(StockTransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * POST /api/transactions
     * Records a new stock transaction and immediately updates inventory levels.
     */
    @PostMapping
    public ResponseEntity<StockTransactionResponse> recordTransaction(@Valid @RequestBody StockTransactionRequest request) {
        StockTransactionResponse response = transactionService.recordTransaction(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * GET /api/transactions
     * Retrieves all recorded transactions (most recent first).
     */
    @GetMapping
    public ResponseEntity<List<StockTransactionResponse>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    /**
     * GET /api/transactions/product/{productId}
     * Retrieves the transaction history for a specific product.
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<StockTransactionResponse>> getTransactionsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(transactionService.getTransactionsByProductId(productId));
    }

    /**
     * GET /api/transactions/recent
     * Retrieves recent transactions for dashboard activity widgets.
     */
    @GetMapping("/recent")
    public ResponseEntity<List<StockTransactionResponse>> getRecentTransactions() {
        return ResponseEntity.ok(transactionService.getRecentTransactions());
    }
}
