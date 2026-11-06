import os
import sys
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

import pytest
import json
from app import app


@pytest.fixture
def client():
    app.config['TESTING'] = True
    with app.test_client() as client:
        yield client


def test_health_check(client):
    response = client.get('/health')
    assert response.status_code == 200
    data = response.get_json()
    assert data['status'] == 'UP'
    assert 'SmartShelfX' in data['service']


def test_valid_forecast(client):
    payload = {
        "productId": 101,
        "daysToForecast": 7,
        "leadTimeDays": 3,
        "currentStock": 20,
        "stockOnOrder": 0,
        "historicalSales": [
            {"date": "2026-10-01", "quantity": 12},
            {"date": "2026-10-02", "quantity": 15},
            {"date": "2026-10-03", "quantity": 14},
            {"date": "2026-10-04", "quantity": 18},
            {"date": "2026-10-05", "quantity": 16},
            {"date": "2026-10-06", "quantity": 20},
            {"date": "2026-10-07", "quantity": 22},
            {"date": "2026-10-08", "quantity": 21},
            {"date": "2026-10-09", "quantity": 25},
            {"date": "2026-10-10", "quantity": 24}
        ]
    }

    response = client.post('/api/forecast',
                           data=json.dumps(payload),
                           content_type='application/json')

    assert response.status_code == 200
    data = response.get_json()
    assert data['productId'] == 101
    assert data['forecastPeriodDays'] == 7
    assert len(data['dailyForecasts']) == 7
    assert data['totalPredictedDemand'] > 0
    assert 'restockingRecommendation' in data
    assert 'modelEvaluation' in data
    assert data['restockingRecommendation']['recommendedOrderQuantity'] >= 0


def test_missing_product_id(client):
    payload = {
        "daysToForecast": 7,
        "historicalSales": [{"date": "2026-10-01", "quantity": 5}]
    }
    response = client.post('/api/forecast',
                           data=json.dumps(payload),
                           content_type='application/json')
    assert response.status_code == 400
    data = response.get_json()
    assert "productId" in data['message']


def test_insufficient_historical_data(client):
    payload = {
        "productId": 102,
        "daysToForecast": 7,
        "historicalSales": [
            {"date": "2026-10-01", "quantity": 5} # only 1 point, requires at least 3
        ]
    }
    response = client.post('/api/forecast',
                           data=json.dumps(payload),
                           content_type='application/json')
    assert response.status_code == 400
    data = response.get_json()
    assert data['error'] == "Insufficient Data"
