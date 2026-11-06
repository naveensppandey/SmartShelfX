package com.smartshelfx.dto;

import java.util.List;
import java.util.Map;

/**
 * ForecastResponse
 * 
 * Response received from the Python AI microservice (or internal Java fallback)
 * containing predictions, model evaluation metrics, and restocking quantities.
 */
public class ForecastResponse {

    private Long productId;
    private String productName;
    private String productSku;
    private int forecastPeriodDays;
    private double totalPredictedDemand;
    private List<Double> dailyForecasts;
    private int historicalDataPoints;
    private Map<String, Object> modelEvaluation;
    private Map<String, Object> restockingRecommendation;
    private String limitationsNote;
    private String sourceService; // "PYTHON_AI_ENGINE" or "JAVA_FALLBACK"

    public ForecastResponse() {
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public int getForecastPeriodDays() {
        return forecastPeriodDays;
    }

    public void setForecastPeriodDays(int forecastPeriodDays) {
        this.forecastPeriodDays = forecastPeriodDays;
    }

    public double getTotalPredictedDemand() {
        return totalPredictedDemand;
    }

    public void setTotalPredictedDemand(double totalPredictedDemand) {
        this.totalPredictedDemand = totalPredictedDemand;
    }

    public List<Double> getDailyForecasts() {
        return dailyForecasts;
    }

    public void setDailyForecasts(List<Double> dailyForecasts) {
        this.dailyForecasts = dailyForecasts;
    }

    public int getHistoricalDataPoints() {
        return historicalDataPoints;
    }

    public void setHistoricalDataPoints(int historicalDataPoints) {
        this.historicalDataPoints = historicalDataPoints;
    }

    public Map<String, Object> getModelEvaluation() {
        return modelEvaluation;
    }

    public void setModelEvaluation(Map<String, Object> modelEvaluation) {
        this.modelEvaluation = modelEvaluation;
    }

    public Map<String, Object> getRestockingRecommendation() {
        return restockingRecommendation;
    }

    public void setRestockingRecommendation(Map<String, Object> restockingRecommendation) {
        this.restockingRecommendation = restockingRecommendation;
    }

    public String getLimitationsNote() {
        return limitationsNote;
    }

    public void setLimitationsNote(String limitationsNote) {
        this.limitationsNote = limitationsNote;
    }

    public String getSourceService() {
        return sourceService;
    }

    public void setSourceService(String sourceService) {
        this.sourceService = sourceService;
    }
}
