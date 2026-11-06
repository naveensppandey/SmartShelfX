package com.smartshelfx.service;

import com.smartshelfx.dto.OrderStatusUpdateRequest;
import com.smartshelfx.dto.PurchaseOrderItemRequest;
import com.smartshelfx.dto.PurchaseOrderRequest;
import com.smartshelfx.dto.PurchaseOrderResponse;
import com.smartshelfx.entity.Product;
import com.smartshelfx.entity.PurchaseOrder;
import com.smartshelfx.entity.PurchaseOrderItem;
import com.smartshelfx.exception.BadRequestException;
import com.smartshelfx.repository.ProductRepository;
import com.smartshelfx.repository.PurchaseOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    @Mock
    private StockTransactionService stockTransactionService;

    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    private Product sampleProduct;
    private PurchaseOrder samplePo;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product();
        sampleProduct.setId(20L);
        sampleProduct.setName("USB-C Hub");
        sampleProduct.setSku("ACC-HUB-020");
        sampleProduct.setCostPrice(new BigDecimal("25.00"));
        sampleProduct.setCurrentStock(10);
        sampleProduct.setMinStockThreshold(15);

        samplePo = new PurchaseOrder();
        samplePo.setId(100L);
        samplePo.setOrderNumber("PO-20261021-1234");
        samplePo.setStatus("APPROVED");

        PurchaseOrderItem item = new PurchaseOrderItem(sampleProduct, 40, new BigDecimal("25.00"));
        item.setId(201L);
        samplePo.addItem(item);
    }

    @Test
    @DisplayName("createPurchaseOrder should create order with PENDING status and correct items")
    void createPurchaseOrder_Success() {
        when(purchaseOrderRepository.existsByOrderNumber(any())).thenReturn(false);
        when(productService.findProductEntityById(20L)).thenReturn(sampleProduct);
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(i -> {
            PurchaseOrder po = i.getArgument(0);
            po.setId(50L);
            return po;
        });

        PurchaseOrderRequest request = new PurchaseOrderRequest();
        request.setVendorId(1L);
        request.setVendorName("Anker Supplies");
        request.setItems(List.of(new PurchaseOrderItemRequest(20L, 30, new BigDecimal("25.00"))));

        PurchaseOrderResponse response = purchaseOrderService.createPurchaseOrder(request);

        assertNotNull(response);
        assertEquals("PENDING", response.getStatus());
        assertEquals(new BigDecimal("750.00"), response.getTotalAmount());
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("updateOrderStatus to RECEIVED should trigger stock transaction for all items")
    void updateOrderStatus_ToReceived_Success() {
        when(purchaseOrderRepository.findById(100L)).thenReturn(Optional.of(samplePo));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(i -> i.getArgument(0));

        OrderStatusUpdateRequest updateReq = new OrderStatusUpdateRequest("RECEIVED", "Verified by warehouse dock");
        PurchaseOrderResponse response = purchaseOrderService.updateOrderStatus(100L, updateReq);

        assertNotNull(response);
        assertEquals("RECEIVED", response.getStatus());
        assertNotNull(response.getReceivedAt());

        // Verify that stock transaction was recorded for the 40 items ordered
        verify(stockTransactionService, times(1)).recordTransaction(argThat(tx ->
                tx.getProductId().equals(20L) &&
                tx.getTransactionType().equals("IN") &&
                tx.getQuantity().equals(40)
        ));
    }

    @Test
    @DisplayName("updateOrderStatus should reject duplicate receiving when already RECEIVED")
    void updateOrderStatus_DuplicateReceiving_Rejected() {
        samplePo.setStatus("RECEIVED");
        when(purchaseOrderRepository.findById(100L)).thenReturn(Optional.of(samplePo));

        OrderStatusUpdateRequest updateReq = new OrderStatusUpdateRequest("RECEIVED", "Duplicate submit");

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> purchaseOrderService.updateOrderStatus(100L, updateReq)
        );

        assertTrue(ex.getMessage().contains("has already been marked as RECEIVED"));
        verify(stockTransactionService, never()).recordTransaction(any());
    }
}
