package com.smartshelfx.service;

import com.smartshelfx.dto.StockTransactionRequest;
import com.smartshelfx.dto.StockTransactionResponse;
import com.smartshelfx.entity.Category;
import com.smartshelfx.entity.Product;
import com.smartshelfx.entity.StockTransaction;
import com.smartshelfx.exception.BadRequestException;
import com.smartshelfx.repository.ProductRepository;
import com.smartshelfx.repository.StockTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockTransactionServiceTest {

    @Mock
    private StockTransactionRepository transactionRepository;

    @Mock
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private StockTransactionService transactionService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        Category category = new Category("Hardware", "Tools and parts");
        category.setId(1L);

        sampleProduct = new Product();
        sampleProduct.setId(5L);
        sampleProduct.setName("Precision Screwdriver Set");
        sampleProduct.setSku("HDW-SCR-005");
        sampleProduct.setCategory(category);
        sampleProduct.setPrice(new BigDecimal("19.99"));
        sampleProduct.setCostPrice(new BigDecimal("10.00"));
        sampleProduct.setCurrentStock(50);
        sampleProduct.setMinStockThreshold(15);
    }

    @Test
    @DisplayName("recordTransaction with type IN should increment product stock")
    void recordTransaction_StockIn_Success() {
        when(productService.findProductEntityById(5L)).thenReturn(sampleProduct);
        when(transactionRepository.save(any(StockTransaction.class))).thenAnswer(i -> {
            StockTransaction tx = i.getArgument(0);
            tx.setId(101L);
            return tx;
        });

        StockTransactionRequest request = new StockTransactionRequest(5L, "IN", 25, "Restock batch");
        StockTransactionResponse response = transactionService.recordTransaction(request);

        assertNotNull(response);
        assertEquals(75, sampleProduct.getCurrentStock()); // 50 + 25
        assertEquals(50, response.getPreviousStock());
        assertEquals(75, response.getNewStock());
        assertEquals("IN", response.getTransactionType());
        verify(productRepository, times(1)).save(sampleProduct);
        verify(transactionRepository, times(1)).save(any(StockTransaction.class));
    }

    @Test
    @DisplayName("recordTransaction with type OUT should deduct product stock when available")
    void recordTransaction_StockOut_Success() {
        when(productService.findProductEntityById(5L)).thenReturn(sampleProduct);
        when(transactionRepository.save(any(StockTransaction.class))).thenAnswer(i -> {
            StockTransaction tx = i.getArgument(0);
            tx.setId(102L);
            return tx;
        });

        StockTransactionRequest request = new StockTransactionRequest(5L, "OUT", 20, "Customer sale");
        StockTransactionResponse response = transactionService.recordTransaction(request);

        assertNotNull(response);
        assertEquals(30, sampleProduct.getCurrentStock()); // 50 - 20
        assertEquals(50, response.getPreviousStock());
        assertEquals(30, response.getNewStock());
        assertEquals("OUT", response.getTransactionType());
        verify(productRepository, times(1)).save(sampleProduct);
    }

    @Test
    @DisplayName("recordTransaction with type OUT should reject when requested quantity exceeds stock")
    void recordTransaction_StockOut_InsufficientStock() {
        when(productService.findProductEntityById(5L)).thenReturn(sampleProduct);

        StockTransactionRequest request = new StockTransactionRequest(5L, "OUT", 60, "Excess sale");

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> transactionService.recordTransaction(request)
        );

        assertTrue(exception.getMessage().contains("Insufficient stock"));
        assertEquals(50, sampleProduct.getCurrentStock()); // Stock remains unchanged
        verify(productRepository, never()).save(any(Product.class));
        verify(transactionRepository, never()).save(any(StockTransaction.class));
    }
}
