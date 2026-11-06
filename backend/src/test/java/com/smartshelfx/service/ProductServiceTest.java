package com.smartshelfx.service;

import com.smartshelfx.dto.ProductRequest;
import com.smartshelfx.dto.ProductResponse;
import com.smartshelfx.entity.Category;
import com.smartshelfx.entity.Product;
import com.smartshelfx.exception.BadRequestException;
import com.smartshelfx.exception.DuplicateResourceException;
import com.smartshelfx.exception.ResourceNotFoundException;
import com.smartshelfx.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ProductService productService;

    private Category sampleCategory;
    private Product sampleProduct;
    private ProductRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleCategory = new Category("Electronics", "Electronic components and devices");
        sampleCategory.setId(1L);

        sampleProduct = new Product();
        sampleProduct.setId(10L);
        sampleProduct.setName("Wireless Mouse");
        sampleProduct.setSku("ELEC-WM-001");
        sampleProduct.setCategory(sampleCategory);
        sampleProduct.setPrice(new BigDecimal("29.99"));
        sampleProduct.setCostPrice(new BigDecimal("15.00"));
        sampleProduct.setCurrentStock(100);
        sampleProduct.setMinStockThreshold(20);
        sampleProduct.setReorderQuantity(50);

        sampleRequest = new ProductRequest();
        sampleRequest.setName("Wireless Mouse");
        sampleRequest.setSku("ELEC-WM-001");
        sampleRequest.setCategoryId(1L);
        sampleRequest.setPrice(new BigDecimal("29.99"));
        sampleRequest.setCostPrice(new BigDecimal("15.00"));
        sampleRequest.setCurrentStock(100);
        sampleRequest.setMinStockThreshold(20);
        sampleRequest.setReorderQuantity(50);
    }

    @Test
    @DisplayName("createProduct should successfully create and return a product when SKU is unique")
    void createProduct_Success() {
        when(productRepository.existsBySkuIgnoreCase(anyString())).thenReturn(false);
        when(categoryService.findCategoryEntityById(1L)).thenReturn(sampleCategory);
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductResponse response = productService.createProduct(sampleRequest);

        assertNotNull(response);
        assertEquals("ELEC-WM-001", response.getSku());
        assertEquals("Wireless Mouse", response.getName());
        assertEquals("IN_STOCK", response.getStockStatus());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("createProduct should throw DuplicateResourceException when SKU already exists")
    void createProduct_DuplicateSku() {
        when(productRepository.existsBySkuIgnoreCase("ELEC-WM-001")).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> productService.createProduct(sampleRequest)
        );

        assertTrue(exception.getMessage().contains("already exists"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("getProductById should throw ResourceNotFoundException when product does not exist")
    void getProductById_NotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(999L));
    }

    @Test
    @DisplayName("updateStock should reject deduction when current stock is insufficient")
    void updateStock_InsufficientStock() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(sampleProduct));

        // Deducting 150 from 100 should fail
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> productService.updateStock(10L, -150)
        );

        assertTrue(exception.getMessage().contains("Current stock is only 100"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("updateStock should succeed when deducting valid quantity")
    void updateStock_ValidDeduction() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product updated = productService.updateStock(10L, -30);

        assertEquals(70, updated.getCurrentStock());
        verify(productRepository, times(1)).save(sampleProduct);
    }
}
