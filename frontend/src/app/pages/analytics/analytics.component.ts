import { Component, OnInit, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AnalyticsService } from '../../core/services/analytics.service';
import { ProductService } from '../../core/services/product.service';
import { DashboardStats, Product } from '../../core/models/models';
import Chart from 'chart.js/auto';

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './analytics.component.html',
  styleUrls: ['./analytics.component.scss']
})
export class AnalyticsComponent implements OnInit, AfterViewInit {
  @ViewChild('categoryValuationCanvas') categoryCanvasRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('stockHealthCanvas') stockHealthCanvasRef!: ElementRef<HTMLCanvasElement>;

  stats: DashboardStats | null = null;
  products: Product[] = [];
  topValueProducts: Product[] = [];
  loading: boolean = true;
  error: string | null = null;

  categoryChart: Chart | null = null;
  stockHealthChart: Chart | null = null;

  totalUnitsInStock: number = 0;
  averageProductPrice: number = 0;

  constructor(
    private analyticsService: AnalyticsService,
    private productService: ProductService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  ngAfterViewInit(): void {
    // Canvas initialized after view
  }

  loadData(): void {
    this.loading = true;
    this.error = null;

    this.analyticsService.getDashboardStats().subscribe({
      next: (statsData: DashboardStats) => {
        this.stats = statsData;

        this.productService.getAllProducts().subscribe({
          next: (prods: Product[]) => {
            this.products = prods;
            this.computeMetrics();
            this.loading = false;
            setTimeout(() => this.renderCharts(), 50);
          },
          error: (err: any) => {
            this.loading = false;
            this.error = 'Failed to load product analytics: ' + (err.error?.message || err.message);
          }
        });
      },
      error: (err: any) => {
        this.loading = false;
        this.error = 'Failed to load analytics: ' + (err.error?.message || err.message);
      }
    });
  }

  computeMetrics(): void {
    if (!this.products.length) return;

    this.totalUnitsInStock = this.products.reduce((acc, p) => acc + p.currentStock, 0);
    const sumPrice = this.products.reduce((acc, p) => acc + p.price, 0);
    this.averageProductPrice = sumPrice / this.products.length;

    // Sort top 5 products by totalInventoryValue
    this.topValueProducts = [...this.products]
      .sort((a, b) => b.totalInventoryValue - a.totalInventoryValue)
      .slice(0, 5);
  }

  renderCharts(): void {
    if (!this.stats || !this.products) return;

    this.renderCategoryValuationChart();
    this.renderStockHealthChart();
  }

  renderCategoryValuationChart(): void {
    if (!this.categoryCanvasRef) return;
    if (this.categoryChart) {
      this.categoryChart.destroy();
    }

    // Group valuation by category
    const catMap: { [cat: string]: number } = {};
    for (const p of this.products) {
      const cat = p.categoryName || 'Uncategorized';
      catMap[cat] = (catMap[cat] || 0) + p.totalInventoryValue;
    }

    const labels = Object.keys(catMap);
    const dataValues = Object.values(catMap);

    const colors = [
      '#2563eb', '#3b82f6', '#60a5fa', '#93c5fd', '#bfdbfe',
      '#10b981', '#f59e0b', '#8b5cf6', '#ec4899'
    ];

    const ctx = this.categoryCanvasRef.nativeElement.getContext('2d');
    if (!ctx) return;

    this.categoryChart = new Chart(ctx, {
      type: 'pie',
      data: {
        labels: labels,
        datasets: [
          {
            data: dataValues,
            backgroundColor: colors.slice(0, labels.length),
            borderWidth: 1
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'right'
          },
          tooltip: {
            callbacks: {
              label: (context) => {
                const val = Number(context.raw) || 0;
                return ` $${val.toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
              }
            }
          }
        }
      }
    });
  }

  renderStockHealthChart(): void {
    if (!this.stockHealthCanvasRef || !this.stats) return;
    if (this.stockHealthChart) {
      this.stockHealthChart.destroy();
    }

    const inStock = this.stats.totalProducts - this.stats.lowStockCount - this.stats.outOfStockCount;
    const lowStock = this.stats.lowStockCount;
    const outStock = this.stats.outOfStockCount;

    const ctx = this.stockHealthCanvasRef.nativeElement.getContext('2d');
    if (!ctx) return;

    this.stockHealthChart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: ['Healthy Stock', 'Low Stock Threshold', 'Out of Stock'],
        datasets: [
          {
            data: [inStock, lowStock, outStock],
            backgroundColor: ['#10b981', '#f59e0b', '#ef4444'],
            borderWidth: 2,
            hoverOffset: 4
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom'
          }
        }
      }
    });
  }

  printReport(): void {
    window.print();
  }
}
