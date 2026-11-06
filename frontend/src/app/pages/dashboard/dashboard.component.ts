import { Component, OnInit, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AnalyticsService } from '../../core/services/analytics.service';
import { DashboardStats } from '../../core/models/models';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit, AfterViewInit {
  @ViewChild('trendChartCanvas') trendChartCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('categoryChartCanvas') categoryChartCanvas!: ElementRef<HTMLCanvasElement>;

  stats: DashboardStats | null = null;
  loading = true;
  errorMessage = '';

  private trendChart: Chart | null = null;
  private categoryChart: Chart | null = null;

  constructor(private analyticsService: AnalyticsService) {}

  ngOnInit(): void {
    this.fetchDashboardData();
  }

  ngAfterViewInit(): void {
    // If stats are already loaded, render charts
    if (this.stats) {
      this.renderCharts();
    }
  }

  fetchDashboardData(): void {
    this.loading = true;
    this.analyticsService.getDashboardStats().subscribe({
      next: (data) => {
        this.stats = data;
        this.loading = false;
        setTimeout(() => this.renderCharts(), 50);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Failed to load live dashboard statistics.';
      }
    });
  }

  private renderCharts(): void {
    if (!this.stats) return;

    // 1. Movement Trends Chart
    if (this.trendChartCanvas && this.stats.movementTrends?.length > 0) {
      if (this.trendChart) this.trendChart.destroy();

      const labels = this.stats.movementTrends.map((t) => t.date.slice(5)); // MM-DD
      const stockInData = this.stats.movementTrends.map((t) => t.stockIn);
      const stockOutData = this.stats.movementTrends.map((t) => t.stockOut);

      this.trendChart = new Chart(this.trendChartCanvas.nativeElement, {
        type: 'bar',
        data: {
          labels,
          datasets: [
            {
              label: 'Stock In (Received)',
              data: stockInData,
              backgroundColor: '#0284c7',
              borderRadius: 4
            },
            {
              label: 'Stock Out (Dispatched)',
              data: stockOutData,
              backgroundColor: '#f97316',
              borderRadius: 4
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
            y: { beginAtZero: true }
          }
        }
      });
    }

    // 2. Category Breakdown Chart
    if (this.categoryChartCanvas && this.stats.categoryProductCount) {
      if (this.categoryChart) this.categoryChart.destroy();

      const catLabels = Object.keys(this.stats.categoryProductCount);
      const catData = Object.values(this.stats.categoryProductCount);

      this.categoryChart = new Chart(this.categoryChartCanvas.nativeElement, {
        type: 'doughnut',
        data: {
          labels: catLabels,
          datasets: [
            {
              data: catData,
              backgroundColor: ['#0284c7', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899', '#64748b']
            }
          ]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { position: 'bottom' }
          }
        }
      });
    }
  }
}
