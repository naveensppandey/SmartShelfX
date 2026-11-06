package com.smartshelfx.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * ForecastRequest
 * 
 * Parameters submitted to initiate AI demand forecasting.
 */
public class ForecastRequest {

    @NotNull(message = "Product ID is required.")
    private Long productId;

    @Min(value = 1, message = "Days to forecast must be at least 1.")
    @Max(value = 90, message = "Days to forecast cannot exceed 90 days.")
    private Integer daysToForecast = 7;

    @Min(value = 1, message = "Lead time must be at least 1 day.")
    @Max(value = 30, message = "Lead time cannot exceed 30 days.")
    private Integer leadTimeDays = 3;

    public ForecastRequest() {
    }

    public ForecastRequest(Long productId, Integer daysToForecast, Integer leadTimeDays) {
        this.productId = productId;
        this.daysToForecast = daysToForecast != null ? daysToForecast : 7;
        this.leadTimeDays = leadTimeDays != null ? leadTimeDays : 3;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getDaysToForecast() {
        return daysToForecast;
    }

    public void setDaysToForecast(Integer daysToForecast) {
        this.daysToForecast = daysToForecast;
    }

    public Integer getLeadTimeDays() {
        return leadTimeDays;
    }

    public void setLeadTimeDays(Integer leadTimeDays) {
        this.leadTimeDays = leadTimeDays;
    }
}
