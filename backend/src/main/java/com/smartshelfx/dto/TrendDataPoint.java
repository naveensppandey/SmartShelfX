package com.smartshelfx.dto;

/**
 * TrendDataPoint
 * 
 * Represents daily aggregated stock movement volume for charts.
 */
public class TrendDataPoint {

    private String date; // YYYY-MM-DD
    private int stockIn;
    private int stockOut;

    public TrendDataPoint() {
    }

    public TrendDataPoint(String date, int stockIn, int stockOut) {
        this.date = date;
        this.stockIn = stockIn;
        this.stockOut = stockOut;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getStockIn() {
        return stockIn;
    }

    public void setStockIn(int stockIn) {
        this.stockIn = stockIn;
    }

    public int getStockOut() {
        return stockOut;
    }

    public void setStockOut(int stockOut) {
        this.stockOut = stockOut;
    }
}
