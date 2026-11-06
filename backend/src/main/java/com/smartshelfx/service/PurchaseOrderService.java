package com.smartshelfx.service;

import com.smartshelfx.dto.*;
import com.smartshelfx.entity.Product;
import com.smartshelfx.entity.PurchaseOrder;
import com.smartshelfx.entity.PurchaseOrderItem;
import com.smartshelfx.exception.BadRequestException;
import com.smartshelfx.exception.ResourceNotFoundException;
import com.smartshelfx.repository.ProductRepository;
import com.smartshelfx.repository.PurchaseOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * PurchaseOrderService
 * 
 * Manages procurement workflows and automated restocking logic.
 * 
 * Crucial Invariant:
 * Receiving a purchase order updates warehouse inventory EXACTLY ONCE.
 * Subsequent attempts to receive an already-received order are rejected.
 */
@Service
@Transactional
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final StockTransactionService stockTransactionService;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                ProductRepository productRepository,
                                ProductService productService,
                                StockTransactionService stockTransactionService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.stockTransactionService = stockTransactionService;
    }

    /**
     * Retrieves all purchase orders ordered by newest first.
     */
    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(PurchaseOrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single purchase order by ID.
     */
    @Transactional(readOnly = true)
    public PurchaseOrderResponse getPurchaseOrderById(Long id) {
        PurchaseOrder po = findPurchaseOrderEntityById(id);
        return PurchaseOrderResponse.fromEntity(po);
    }

    /**
     * Helper to load raw PurchaseOrder entity.
     */
    @Transactional(readOnly = true)
    public PurchaseOrder findPurchaseOrderEntityById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order", "id", id));
    }

    /**
     * Creates a new Purchase Order in PENDING status.
     */
    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request) {
        PurchaseOrder po = new PurchaseOrder();
        po.setOrderNumber(generateOrderNumber());
        po.setVendorId(request.getVendorId());
        po.setVendorName(request.getVendorName());
        po.setNotes(request.getNotes());
        po.setCreatedBy(request.getCreatedBy());
        po.setCreatedByName(request.getCreatedByName() != null ? request.getCreatedByName() : "System");
        po.setStatus("PENDING");

        for (PurchaseOrderItemRequest itemRequest : request.getItems()) {
            Product product = productService.findProductEntityById(itemRequest.getProductId());
            PurchaseOrderItem item = new PurchaseOrderItem(
                    product,
                    itemRequest.getQuantityOrdered(),
                    itemRequest.getUnitCost()
            );
            po.addItem(item);
        }

        PurchaseOrder savedPo = purchaseOrderRepository.save(po);
        return PurchaseOrderResponse.fromEntity(savedPo);
    }

    /**
     * Updates order status with strict lifecycle validation and inventory synchronization.
     * 
     * Supported transitions:
     * - PENDING -> APPROVED
     * - PENDING -> REJECTED
     * - APPROVED -> RECEIVED (applies inventory increase exactly once)
     */
    public PurchaseOrderResponse updateOrderStatus(Long id, OrderStatusUpdateRequest request) {
        PurchaseOrder po = findPurchaseOrderEntityById(id);
        String currentStatus = po.getStatus();
        String requestedStatus = request.getStatus().trim().toUpperCase();

        // 1. Guard against duplicate receiving
        if ("RECEIVED".equalsIgnoreCase(currentStatus)) {
            throw new BadRequestException("Purchase Order " + po.getOrderNumber() +
                    " has already been marked as RECEIVED. Inventory has already been updated.");
        }

        // 2. Guard against resurrecting rejected orders
        if ("REJECTED".equalsIgnoreCase(currentStatus)) {
            throw new BadRequestException("Purchase Order " + po.getOrderNumber() +
                    " has been REJECTED and cannot transition to " + requestedStatus + ".");
        }

        // 3. Validate state machine transitions
        if ("RECEIVED".equalsIgnoreCase(requestedStatus)) {
            if (!"APPROVED".equalsIgnoreCase(currentStatus)) {
                throw new BadRequestException("Only APPROVED purchase orders can be marked as RECEIVED. Current status: " + currentStatus);
            }

            // Execute receiving: increase product stocks and record audit transactions
            for (PurchaseOrderItem item : po.getItems()) {
                Product product = item.getProduct();
                StockTransactionRequest txRequest = new StockTransactionRequest(
                        product.getId(),
                        "IN",
                        item.getQuantityOrdered(),
                        "Received from Purchase Order " + po.getOrderNumber()
                );
                txRequest.setUserId(request.getUserId());
                txRequest.setUserName(request.getUserName() != null ? request.getUserName() : "Warehouse Staff");

                stockTransactionService.recordTransaction(txRequest);
            }

            po.setReceivedAt(LocalDateTime.now());
        }

        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            po.setNotes((po.getNotes() != null ? po.getNotes() + " | " : "") + request.getNotes().trim());
        }

        po.setStatus(requestedStatus);
        PurchaseOrder updatedPo = purchaseOrderRepository.save(po);
        return PurchaseOrderResponse.fromEntity(updatedPo);
    }

    /**
     * Generates restocking suggestions for products currently below their threshold.
     */
    @Transactional(readOnly = true)
    public List<RestockSuggestionResponse> getRestockSuggestions() {
        List<Product> lowStockProducts = productRepository.findLowStockProducts();
        List<Product> outOfStockProducts = productRepository.findOutOfStockProducts();

        List<Product> candidateProducts = new ArrayList<>(outOfStockProducts);
        candidateProducts.addAll(lowStockProducts);

        List<RestockSuggestionResponse> suggestions = new ArrayList<>();

        for (Product product : candidateProducts) {
            RestockSuggestionResponse suggestion = new RestockSuggestionResponse();
            suggestion.setProductId(product.getId());
            suggestion.setProductName(product.getName());
            suggestion.setProductSku(product.getSku());

            if (product.getCategory() != null) {
                suggestion.setCategoryName(product.getCategory().getName());
            }

            suggestion.setVendorId(product.getVendorId());
            suggestion.setVendorName(product.getVendorName());
            suggestion.setCurrentStock(product.getCurrentStock());
            suggestion.setMinStockThreshold(product.getMinStockThreshold());
            suggestion.setReorderQuantity(product.getReorderQuantity());
            suggestion.setUnitCost(product.getCostPrice());

            // Suggested Order Quantity calculation:
            // Ensures safety buffer by replenishing at least the standard batch size
            int neededToBuffer = (product.getMinStockThreshold() * 2) - product.getCurrentStock();
            int suggestedQty = Math.max(product.getReorderQuantity(), Math.max(10, neededToBuffer));
            suggestion.setSuggestedOrderQuantity(suggestedQty);

            if (product.getCostPrice() != null) {
                suggestion.setEstimatedTotalCost(product.getCostPrice().multiply(BigDecimal.valueOf(suggestedQty)));
            } else {
                suggestion.setEstimatedTotalCost(BigDecimal.ZERO);
            }

            if (product.getCurrentStock() <= 0) {
                suggestion.setPriority("HIGH");
                suggestion.setReason("Product is completely out of stock. Immediate replenishment required to avoid lost sales.");
            } else {
                suggestion.setPriority("MEDIUM");
                suggestion.setReason("Product stock (" + product.getCurrentStock() +
                        ") is below minimum threshold (" + product.getMinStockThreshold() + ").");
            }

            suggestions.add(suggestion);
        }

        return suggestions;
    }

    /**
     * Generates a readable unique PO identifier (e.g., PO-20261021-4821).
     */
    private String generateOrderNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String poNumber;
        do {
            int randomCode = ThreadLocalRandom.current().nextInt(1000, 9999);
            poNumber = "PO-" + datePart + "-" + randomCode;
        } while (purchaseOrderRepository.existsByOrderNumber(poNumber));

        return poNumber;
    }
}
