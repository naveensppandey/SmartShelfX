package com.smartshelfx.service;

import com.smartshelfx.dto.ForecastRequest;
import com.smartshelfx.dto.ForecastResponse;
import com.smartshelfx.entity.Product;
import com.smartshelfx.repository.StockTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForecastServiceTest {

    @Mock
    private ProductService productService;

    @Mock
    private StockTransactionRepository transactionRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ForecastService forecastService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product();
        sampleProduct.setId(10L);
        sampleProduct.setName("Ergonomic Keyboard");
        sampleProduct.setSku("KEY-ERG-010");
        sampleProduct.setCurrentStock(20);
        sampleProduct.setMinStockThreshold(10);
        sampleProduct.setCostPrice(new BigDecimal("45.00"));
    }

    @Test
    @DisplayName("generateForecast should gracefully activate Java fallback when AI service is unreachable")
    void generateForecast_PythonServiceOffline_UsesJavaFallback() {
        when(productService.findProductEntityById(10L)).thenReturn(sampleProduct);
        when(transactionRepository.findByProductIdOrderByTransactionDateDesc(10L)).thenReturn(Collections.emptyList());
        when(restTemplate.postForEntity(anyString(), any(), any())).thenThrow(new ResourceAccessException("Connection refused: connect"));

        ForecastRequest request = new ForecastRequest(10L, 7, 3);
        ForecastResponse response = forecastService.generateForecast(request);

        assertNotNull(response);
        assertEquals("JAVA_FALLBACK", response.getSourceService());
        assertEquals(7, response.getForecastPeriodDays());
        assertNotNull(response.getDailyForecasts());
        assertEquals(7, response.getDailyForecasts().size());
        assertTrue(response.getTotalPredictedDemand() > 0);
        assertNotNull(response.getRestockingRecommendation());
    }

    @Test
    @DisplayName("generateForecast should map and return Python AI response when service is online")
    void generateForecast_PythonServiceOnline_ReturnsAiPrediction() {
        when(productService.findProductEntityById(10L)).thenReturn(sampleProduct);
        when(transactionRepository.findByProductIdOrderByTransactionDateDesc(10L)).thenReturn(Collections.emptyList());

        Map<String, Object> mockAiResponseBody = Map.of(
                "productId", 10,
                "forecastPeriodDays", 7,
                "totalPredictedDemand", 84.5,
                "dailyForecasts", List.of(12.0, 12.0, 12.0, 12.0, 12.0, 12.0, 12.5),
                "historicalDataPoints", 14,
                "modelEvaluation", Map.of("modelType", "Linear Regression", "meanAbsoluteError", 1.2),
                "restockingRecommendation", Map.of("recommendedOrderQuantity", 45, "restockUrgency", "HIGH"),
                "limitationsNote", "Linear trend assumption"
        );

        ResponseEntity responseEntity = new ResponseEntity<>(mockAiResponseBody, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class))).thenReturn(responseEntity);

        ForecastRequest request = new ForecastRequest(10L, 7, 3);
        ForecastResponse response = forecastService.generateForecast(request);

        assertNotNull(response);
        assertEquals("PYTHON_AI_ENGINE", response.getSourceService());
        assertEquals(84.5, response.getTotalPredictedDemand());
        assertEquals(7, response.getDailyForecasts().size());
        assertEquals(45, ((Map<?, ?>) response.getRestockingRecommendation()).get("recommendedOrderQuantity"));
    }
}
