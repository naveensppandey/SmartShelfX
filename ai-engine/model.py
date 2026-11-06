"""
SmartShelfX AI Forecasting Engine - Model Module

This module implements:
1. Historical demand data cleaning and chronological preparation using Pandas.
2. Trend-based demand forecasting using Linear Regression from Scikit-Learn.
3. Chronological train/test evaluation (Mean Absolute Error & Root Mean Squared Error).
4. Benchmark comparison against a Moving Average baseline.
5. Deterministic inventory restocking recommendation formula:
   Recommended Order Quantity = max(0, (lead_time_demand + safety_stock) - current_stock - stock_on_order)
"""

import numpy as np
import pandas as pd
from sklearn.linear_model import LinearRegression
from sklearn.metrics import mean_absolute_error, mean_squared_error


class DemandForecaster:
    """
    Beginner-friendly, transparent forecasting class.
    Uses ordinary least squares linear regression over chronological day indices.
    """

    def __init__(self, days_to_forecast=7, lead_time_days=3):
        self.days_to_forecast = days_to_forecast
        self.lead_time_days = lead_time_days

    def prepare_data(self, sales_data):
        """
        Converts raw JSON sales history into a cleaned, chronologically sorted DataFrame.
        Expected format of each element: {'date': 'YYYY-MM-DD', 'quantity': 10}
        """
        df = pd.DataFrame(sales_data)

        if 'date' not in df.columns or 'quantity' not in df.columns:
            raise ValueError("Sales records must contain 'date' and 'quantity' fields.")

        # 1. Parse date and ensure numeric quantity
        df['date'] = pd.to_datetime(df['date'])
        df['quantity'] = pd.to_numeric(df['quantity'], errors='coerce').fillna(0)

        # 2. Group by date in case multiple transactions occurred on the same day
        daily_df = df.groupby('date')['quantity'].sum().reset_index()

        # 3. Sort chronologically
        daily_df = daily_df.sort_values('date').reset_index(drop=True)

        # 4. Generate sequential day index feature (0, 1, 2, ... N-1) for regression
        daily_df['day_index'] = np.arange(len(daily_df))

        return daily_df

    def train_and_forecast(self, sales_data, current_stock=0, stock_on_order=0):
        """
        Main execution flow:
        1. Validate data sufficiency.
        2. Clean and format chronological time series.
        3. Train Linear Regression model with chronological train/test split.
        4. Predict future demand for N days.
        5. Calculate restocking recommendation.
        """
        daily_df = self.prepare_data(sales_data)
        n_points = len(daily_df)

        if n_points < 3:
            raise ValueError(
                f"Insufficient historical data: {n_points} data points provided. "
                f"At least 3 daily records are required to calculate trend projections."
            )

        # Chronological train/test split (use last 20% or minimum 1 point for test)
        test_size = max(1, int(n_points * 0.2)) if n_points >= 5 else 1
        train_df = daily_df.iloc[:-test_size]
        test_df = daily_df.iloc[-test_size:]

        X_train = train_df[['day_index']]
        y_train = train_df['quantity']

        X_test = test_df[['day_index']]
        y_test = test_df['quantity']

        # Train Linear Regression model
        model = LinearRegression()
        model.fit(X_train, y_train)

        # Evaluate model performance on unseen test data
        test_preds = np.clip(model.predict(X_test), 0, None) # clip at 0 because demand cannot be negative
        mae = float(mean_absolute_error(y_test, test_preds))
        rmse = float(np.sqrt(mean_squared_error(y_test, test_preds)))

        # Baseline: Simple Moving Average (mean of training data)
        baseline_pred = np.full(len(y_test), y_train.mean())
        baseline_mae = float(mean_absolute_error(y_test, baseline_pred))

        # Predict future N days
        last_day_index = daily_df['day_index'].max()
        future_indices = pd.DataFrame({
            'day_index': np.arange(last_day_index + 1, last_day_index + 1 + self.days_to_forecast)
        })

        raw_future_preds = model.predict(future_indices)
        daily_forecasts = [max(0.0, round(float(val), 2)) for val in raw_future_preds]
        total_predicted_demand = round(sum(daily_forecasts), 2)

        # Calculate restocking recommendations using standard formula
        restock_info = self.calculate_restock(
            total_predicted_demand=total_predicted_demand,
            current_stock=current_stock,
            stock_on_order=stock_on_order
        )

        return {
            "forecastPeriodDays": self.days_to_forecast,
            "totalPredictedDemand": total_predicted_demand,
            "dailyForecasts": daily_forecasts,
            "historicalDataPoints": n_points,
            "modelEvaluation": {
                "modelType": "Linear Regression (Chronological Split)",
                "testSetSize": test_size,
                "meanAbsoluteError": round(mae, 2),
                "rootMeanSquaredError": round(rmse, 2),
                "baselineMovingAverageMae": round(baseline_mae, 2),
                "modelAccuracyNote": (
                    "Linear regression outperforms moving average baseline"
                    if mae <= baseline_mae
                    else "Historical variance is high; moving average baseline is competitive"
                )
            },
            "restockingRecommendation": restock_info,
            "limitationsNote": "Linear model assumes continuous sales momentum. Excludes sudden seasonal spikes or marketing promotions."
        }

    def calculate_restock(self, total_predicted_demand, current_stock, stock_on_order):
        """
        Formula:
        Recommended Order Quantity = max(0, predicted_demand_lead_time + safety_stock - current_stock - stock_on_order)

        Definitions:
        - predicted_demand_lead_time: Expected units sold while waiting for supplier delivery (lead time).
        - safety_stock: Reserve buffer to guard against supplier delays or demand surges.
        - current_stock: Physical units available in warehouse.
        - stock_on_order: Units already ordered from supplier but not yet received.
        """
        avg_daily_demand = max(0.0, total_predicted_demand / self.days_to_forecast)

        # Demand during supplier delivery lead time (e.g. 3 days)
        lead_time_demand = avg_daily_demand * self.lead_time_days

        # Safety stock buffer = 2 days of average daily demand (minimum 5 units)
        safety_stock = max(5.0, avg_daily_demand * 2)

        net_required = (lead_time_demand + safety_stock) - current_stock - stock_on_order
        recommended_quantity = max(0, int(round(net_required)))

        urgency = "NONE"
        if recommended_quantity > 0:
            if current_stock <= 0:
                urgency = "CRITICAL"
            elif current_stock < lead_time_demand:
                urgency = "HIGH"
            else:
                urgency = "MEDIUM"

        return {
            "leadTimeDays": self.lead_time_days,
            "predictedDemandDuringLeadTime": round(lead_time_demand, 2),
            "safetyStockBuffer": round(safety_stock, 2),
            "currentWarehouseStock": current_stock,
            "stockAlreadyOnOrder": stock_on_order,
            "recommendedOrderQuantity": recommended_quantity,
            "restockUrgency": urgency,
            "explanation": (
                f"Based on an estimated daily demand of {round(avg_daily_demand, 2)} units over a "
                f"{self.lead_time_days}-day lead time, plus {round(safety_stock, 2)} units safety buffer, "
                f"warehouse should order {recommended_quantity} units."
            )
        }
