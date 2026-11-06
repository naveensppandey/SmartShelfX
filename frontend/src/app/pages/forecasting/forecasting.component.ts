import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ForecastService } from '../../core/services/forecast.service';
import { ProductService } from '../../core/services/product.service';
import { OrderService } from '../../core/services/order.service';
import { Product, ForecastResponse } from '../../core/models/models';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-forecasting',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './forecasting.component.html',
  styleUrls: ['./forecasting.component.scss']
})
export class ForecastingComponent implements OnInit {
  @ViewChild('forecastChartCanvas') forecastChartCanvas!: ElementRef<HTMLCanvasElement>;

  products: Product[] = [];
  selectedProductId: number | null = null;
  daysToForecast = 7;
  leadTimeDays = 3;

  loading = false;
  loadingProducts = true;
  forecastResult: ForecastResponse | null = null;
  errorMessage = '';
  poSuccessMessage = '';
  creatingPO = false;

  private chart: Chart | null = null;

  constructor(
    private forecastService: ForecastService,
    private productService: ProductService,
    private orderService: OrderService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.productService.getProducts().subscribe({
      next: (data) => {
        this.products = data;
        this.loadingProducts = false;

        // Check if query param productId was passed (e.g. from dashboard or products list)
        this.route.queryParams.subscribe((params) => {
          if (params['productId']) {
            this.selectedProductId = +params['productId'];
            this.runForecast();
          } else if (this.products.length > 0) {
            this.selectedProductId = this.products[0].id;
          }
        });
      },
      error: () => {
        this.loadingProducts = false;
        this.errorMessage = 'Failed to load products list.';
      }
    });
  }

  getSelectedProduct(): Product | undefined {
    return this.products.find((p) => p.id === this.selectedProductId);
  }

  runForecast(): void {
    if (!this.selectedProductId) return;

    this.loading = true;
    this.errorMessage = '';
    this.poSuccessMessage = '';

    this.forecastService
      .generateCustomForecast({
        productId: this.selectedProductId,
        daysToForecast: this.daysToForecast,
        leadTimeDays: this.leadTimeDays
      })
      .subscribe({
        next: (data) => {
          this.forecastResult = data;
          this.loading = false;
          setTimeout(() => this.renderForecastChart(), 50);
        },
        error: (err) => {
          this.loading = false;
          this.errorMessage = err.error?.message || 'Failed to generate demand forecast.';
        }
      });
  }

  createPoFromSuggestion(): void {
    if (!this.forecastResult || !this.forecastResult.restockingRecommendation) return;

    const qty = this.forecastResult.restockingRecommendation.recommendedOrderQuantity;
    if (qty <= 0) {
      this.errorMessage = 'Recommended order quantity is zero. No purchase order required.';
      return;
    }

    const prod = this.getSelectedProduct();
    if (!prod) return;

    this.creatingPO = true;
    this.errorMessage = '';

    const payload = {
      vendorId: prod.vendorId,
      vendorName: prod.vendorName || 'Primary Supplier',
      notes: `Auto-generated from AI Demand Forecast (Horizon: ${this.daysToForecast} days, Lead Time: ${this.leadTimeDays} days)`,
      items: [
        {
          productId: prod.id,
          quantityOrdered: qty,
          unitCost: prod.costPrice
        }
      ]
    };

    this.orderService.createPurchaseOrder(payload).subscribe({
      next: (po) => {
        this.creatingPO = false;
        this.poSuccessMessage = `Purchase Order ${po.orderNumber} created successfully! You can review and approve it in Purchase Orders.`;
        setTimeout(() => this.router.navigate(['/purchase-orders']), 1800);
      },
      error: (err) => {
        this.creatingPO = false;
        this.errorMessage = err.error?.message || 'Failed to create purchase order.';
      }
    });
  }

  private renderForecastChart(): void {
    if (!this.forecastResult || !this.forecastChartCanvas) return;
    if (this.chart) this.chart.destroy();

    const dailyData = this.forecastResult.dailyForecasts || [];
    const labels = dailyData.map((_, i) => `Day +${i + 1}`);

    this.chart = new Chart(this.forecastChartCanvas.nativeElement, {
      type: 'line',
      data: {
        labels,
        datasets: [
          {
            label: 'Projected Daily Demand (Units)',
            data: dailyData,
            borderColor: '#0284c7',
            backgroundColor: 'rgba(2, 132, 199, 0.1)',
            fill: true,
            tension: 0.3,
            borderWidth: 3,
            pointBackgroundColor: '#0284c7',
            pointRadius: 4
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'top' }
        },
        scales: {
          y: {
            beginAtZero: true,
            title: { display: true, text: 'Units' }
          }
        }
      }
    });
  }
}
