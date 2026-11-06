package com.smartshelfx.service;

import com.smartshelfx.dto.StockTransactionRequest;
import com.smartshelfx.dto.StockTransactionResponse;
import com.smartshelfx.entity.Product;
import com.smartshelfx.entity.StockTransaction;
import com.smartshelfx.exception.BadRequestException;
import com.smartshelfx.repository.ProductRepository;
import com.smartshelfx.repository.StockTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * StockTransactionService
 * 
 * Manages inventory movements and maintains strict stock consistency.
 * 
 * Business Rules:
 * 1. Transaction type must be 'IN' or 'OUT'.
 * 2. An 'OUT' transaction cannot deduct more units than currently in stock (no negative stock).
 * 3. The product stock update and transaction audit log are committed atomically within
 *    a single database transaction (@Transactional).
 */
@Service
@Transactional
public class StockTransactionService {

    private final StockTransactionRepository transactionRepository;
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public StockTransactionService(StockTransactionRepository transactionRepository,
                                   ProductService productService,
                                   ProductRepository productRepository,
                                   NotificationService notificationService) {
        this.transactionRepository = transactionRepository;
        this.productService = productService;
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }

    /**
     * Records a stock movement (IN or OUT) and updates the product's inventory balance.
     */
    public StockTransactionResponse recordTransaction(StockTransactionRequest request) {
        Product product = productService.findProductEntityById(request.getProductId());

        int previousStock = product.getCurrentStock();
        int quantity = request.getQuantity();
        int newStock;

        String type = request.getTransactionType().trim().toUpperCase();

        if ("IN".equals(type)) {
            newStock = previousStock + quantity;
        } else if ("OUT".equals(type)) {
            if (previousStock < quantity) {
                throw new BadRequestException(String.format(
                        "Insufficient stock for product '%s' (SKU: %s). Requested deduction: %d, Available: %d.",
                        product.getName(), product.getSku(), quantity, previousStock
                ));
            }
            newStock = previousStock - quantity;
        } else {
            throw new BadRequestException("Invalid transaction type '" + type + "'. Must be 'IN' or 'OUT'.");
        }

        // 1. Update product's current stock
        product.setCurrentStock(newStock);
        productRepository.save(product);

        // Check if low-stock alert should be created
        notificationService.checkAndTriggerStockAlert(product);

        // 2. Create and persist audit record
        StockTransaction transaction = new StockTransaction();
        transaction.setProduct(product);
        transaction.setTransactionType(type);
        transaction.setQuantity(quantity);
        transaction.setPreviousStock(previousStock);
        transaction.setNewStock(newStock);
        transaction.setNotes(request.getNotes());
        transaction.setUserId(request.getUserId());
        transaction.setUserName(request.getUserName() != null ? request.getUserName() : "System");
        transaction.setTransactionDate(LocalDateTime.now());

        StockTransaction savedTx = transactionRepository.save(transaction);
        return StockTransactionResponse.fromEntity(savedTx);
    }

    /**
     * Returns full movement history for a specific product.
     */
    @Transactional(readOnly = true)
    public List<StockTransactionResponse> getTransactionsByProductId(Long productId) {
        // Verify product exists first
        productService.findProductEntityById(productId);

        return transactionRepository.findByProductIdOrderByTransactionDateDesc(productId).stream()
                .map(StockTransactionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Returns all stock transactions across the system (audit log).
     */
    @Transactional(readOnly = true)
    public List<StockTransactionResponse> getAllTransactions() {
        return transactionRepository.findAllByOrderByTransactionDateDesc().stream()
                .map(StockTransactionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Returns the 10 most recent transactions for dashboard activity feeds.
     */
    @Transactional(readOnly = true)
    public List<StockTransactionResponse> getRecentTransactions() {
        return transactionRepository.findRecentTransactions().stream()
                .map(StockTransactionResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
