package com.smartshelfx.controller;

import com.smartshelfx.dto.DashboardStatsResponse;
import com.smartshelfx.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AnalyticsController
 * 
 * REST API endpoints exposing aggregated inventory statistics,
 * valuation, and movement trends.
 * Base Path: /api/analytics
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * GET /api/analytics/dashboard
     * Returns unified dashboard statistics including inventory value,
     * low-stock alerts, categories breakdown, and 14-day movement charts.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(analyticsService.getDashboardStats());
    }
}
