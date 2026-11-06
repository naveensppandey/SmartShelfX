package com.smartshelfx.controller;

import com.smartshelfx.dto.OrderStatusUpdateRequest;
import com.smartshelfx.dto.PurchaseOrderRequest;
import com.smartshelfx.dto.PurchaseOrderResponse;
import com.smartshelfx.dto.RestockSuggestionResponse;
import com.smartshelfx.service.PurchaseOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PurchaseOrderController
 * 
 * REST API endpoints for placing, reviewing, approving, receiving,
 * and generating suggestions for purchase orders.
 * Base Path: /api/purchase-orders
 */
@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    /**
     * GET /api/purchase-orders
     * Retrieves all purchase orders.
     */
    @GetMapping
    public ResponseEntity<List<PurchaseOrderResponse>> getAllPurchaseOrders() {
        return ResponseEntity.ok(purchaseOrderService.getAllPurchaseOrders());
    }

    /**
     * GET /api/purchase-orders/{id}
     * Retrieves details and line items for a specific purchase order.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponse> getPurchaseOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrderById(id));
    }

    /**
     * POST /api/purchase-orders
     * Creates a new purchase order.
     */
    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> createPurchaseOrder(@Valid @RequestBody PurchaseOrderRequest request) {
        PurchaseOrderResponse created = purchaseOrderService.createPurchaseOrder(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * PUT /api/purchase-orders/{id}/status
     * Transitions order status (e.g., APPROVED, REJECTED, RECEIVED).
     * Receiving automatically updates warehouse inventory stock.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<PurchaseOrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(purchaseOrderService.updateOrderStatus(id, request));
    }

    /**
     * GET /api/purchase-orders/suggestions
     * Returns automated restocking suggestions based on low and out-of-stock items.
     */
    @GetMapping("/suggestions")
    public ResponseEntity<List<RestockSuggestionResponse>> getRestockSuggestions() {
        return ResponseEntity.ok(purchaseOrderService.getRestockSuggestions());
    }
}
