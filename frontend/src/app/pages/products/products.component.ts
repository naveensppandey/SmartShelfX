import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../core/services/product.service';
import { CategoryService } from '../../core/services/category.service';
import { Product, Category } from '../../core/models/models';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './products.component.html',
  styleUrls: ['./products.component.scss']
})
export class ProductsComponent implements OnInit {
  products: Product[] = [];
  categories: Category[] = [];
  loading = true;
  errorMessage = '';
  successMessage = '';

  // Filter criteria
  searchKeyword = '';
  selectedCategoryId: number | null = null;
  selectedStockStatus = 'ALL';

  // Deletion modal state
  productToDelete: Product | null = null;
  deleting = false;

  constructor(
    private productService: ProductService,
    private categoryService: CategoryService
  ) {}

  ngOnInit(): void {
    this.loadCategories();
    this.loadProducts();
  }

  loadCategories(): void {
    this.categoryService.getCategories().subscribe({
      next: (data) => (this.categories = data),
      error: () => console.error('Failed to load categories')
    });
  }

  loadProducts(): void {
    this.loading = true;
    this.productService
      .getProducts(
        this.searchKeyword,
        this.selectedCategoryId || undefined,
        this.selectedStockStatus
      )
      .subscribe({
        next: (data) => {
          this.products = data;
          this.loading = false;
        },
        error: (err) => {
          this.loading = false;
          this.errorMessage = err.error?.message || 'Failed to load products from database.';
        }
      });
  }

  onFilterChange(): void {
    this.loadProducts();
  }

  resetFilters(): void {
    this.searchKeyword = '';
    this.selectedCategoryId = null;
    this.selectedStockStatus = 'ALL';
    this.loadProducts();
  }

  confirmDelete(product: Product): void {
    this.productToDelete = product;
  }

  cancelDelete(): void {
    this.productToDelete = null;
  }

  executeDelete(): void {
    if (!this.productToDelete) return;

    this.deleting = true;
    this.productService.deleteProduct(this.productToDelete.id).subscribe({
      next: () => {
        this.deleting = false;
        this.successMessage = `Product '${this.productToDelete?.name}' deleted successfully.`;
        this.productToDelete = null;
        this.loadProducts();
        setTimeout(() => (this.successMessage = ''), 3000);
      },
      error: (err) => {
        this.deleting = false;
        this.errorMessage = err.error?.message || 'Failed to delete product.';
        this.productToDelete = null;
      }
    });
  }
}
