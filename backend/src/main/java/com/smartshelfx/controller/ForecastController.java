package com.smartshelfx.controller;

import com.smartshelfx.dto.ForecastRequest;
import com.smartshelfx.dto.ForecastResponse;
import com.smartshelfx.service.ForecastService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ForecastController
 * 
 * REST API endpoints for demand forecasting and inventory replenishment.
 * Base Path: /api/forecast
 */
@RestController
@RequestMapping("/api/forecast")
public class ForecastController {

    private final ForecastService forecastService;

    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    /**
     * POST /api/forecast
     * Triggers AI model forecasting for a product with configurable days and lead time.
     */
    @PostMapping
    public ResponseEntity<ForecastResponse> generateForecast(@Valid @RequestBody ForecastRequest request) {
        return ResponseEntity.ok(forecastService.generateForecast(request));
    }

    /**
     * GET /api/forecast/product/{productId}
     * Convenience endpoint returning a default 7-day forecast for a single product.
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ForecastResponse> getProductForecast(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "7") Integer days,
            @RequestParam(defaultValue = "3") Integer leadTime) {
        ForecastRequest request = new ForecastRequest(productId, days, leadTime);
        return ResponseEntity.ok(forecastService.generateForecast(request));
    }
}
