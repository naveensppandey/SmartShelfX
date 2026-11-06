"""
SmartShelfX AI Forecasting Engine - Flask REST Microservice
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
from model import DemandForecaster

app = Flask(__name__)
CORS(app)


@app.route('/health', methods=['GET'])
def health_check():
    """
    Health check endpoint for monitoring service readiness.
    """
    return jsonify({
        "status": "UP",
        "service": "SmartShelfX AI Forecasting Engine",
        "version": "1.0.0",
        "framework": "Flask 3.x / Scikit-learn"
    }), 200


@app.route('/api/forecast', methods=['POST'])
def forecast_demand():
    """
    POST /api/forecast
    
    Accepts historical sales data and returns machine-learning projected demand
    along with calculated restocking orders.
    """
    data = request.get_json()

    if not data:
        return jsonify({
            "error": "Bad Request",
            "message": "Missing JSON request body."
        }), 400

    product_id = data.get('productId')
    sales_history = data.get('historicalSales')

    if not product_id:
        return jsonify({
            "error": "Bad Request",
            "message": "Field 'productId' is required."
        }), 400

    if not sales_history or not isinstance(sales_history, list):
        return jsonify({
            "error": "Bad Request",
            "message": "Field 'historicalSales' must be a non-empty array of records containing 'date' and 'quantity'."
        }), 400

    days_to_forecast = int(data.get('daysToForecast', 7))
    if days_to_forecast < 1 or days_to_forecast > 90:
        return jsonify({
            "error": "Bad Request",
            "message": "Field 'daysToForecast' must be between 1 and 90 days."
        }), 400

    lead_time_days = int(data.get('leadTimeDays', 3))
    current_stock = int(data.get('currentStock', 0))
    stock_on_order = int(data.get('stockOnOrder', 0))

    try:
        forecaster = DemandForecaster(
            days_to_forecast=days_to_forecast,
            lead_time_days=lead_time_days
        )

        forecast_result = forecaster.train_and_forecast(
            sales_data=sales_history,
            current_stock=current_stock,
            stock_on_order=stock_on_order
        )

        forecast_result['productId'] = product_id
        return jsonify(forecast_result), 200

    except ValueError as ve:
        return jsonify({
            "error": "Insufficient Data",
            "message": str(ve)
        }), 400
    except Exception as e:
        return jsonify({
            "error": "Internal Processing Error",
            "message": f"Failed to calculate forecast: {str(e)}"
        }), 500


if __name__ == '__main__':
    # Run on port 5000 for local communication with Spring Boot backend
    print("Starting SmartShelfX AI Forecasting Engine on port 5000...")
    app.run(host='0.0.0.0', port=5000, debug=False)
