import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TransactionService } from '../../core/services/transaction.service';
import { ProductService } from '../../core/services/product.service';
import { AuthService } from '../../core/services/auth.service';
import { StockTransaction, Product } from '../../core/models/models';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './transactions.component.html',
  styleUrls: ['./transactions.component.scss']
})
export class TransactionsComponent implements OnInit {
  transactions: StockTransaction[] = [];
  products: Product[] = [];
  loading = true;
  errorMessage = '';
  successMessage = '';

  // Filter
  filterType = 'ALL';
  searchKeyword = '';

  // Modal State
  showModal = false;
  submitting = false;
  selectedProductId: number | null = null;
  selectedType: 'IN' | 'OUT' = 'IN';
  quantity: number = 10;
  notes: string = '';

  constructor(
    private transactionService: TransactionService,
    private productService: ProductService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadProducts();
    this.loadTransactions();
  }

  loadProducts(): void {
    this.productService.getProducts().subscribe({
      next: (data) => (this.products = data),
      error: () => console.error('Failed to load products for transactions')
    });
  }

  loadTransactions(): void {
    this.loading = true;
    this.transactionService.getAllTransactions().subscribe({
      next: (data) => {
        this.transactions = data;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Failed to load stock transactions.';
      }
    });
  }

  get filteredTransactions(): StockTransaction[] {
    return this.transactions.filter((tx) => {
      const matchesType = this.filterType === 'ALL' || tx.transactionType === this.filterType;
      const matchesKeyword =
        !this.searchKeyword ||
        tx.productName?.toLowerCase().includes(this.searchKeyword.toLowerCase()) ||
        tx.productSku?.toLowerCase().includes(this.searchKeyword.toLowerCase());
      return matchesType && matchesKeyword;
    });
  }

  openRecordModal(type: 'IN' | 'OUT' = 'IN', product?: Product): void {
    this.selectedType = type;
    this.selectedProductId = product ? product.id : (this.products[0]?.id || null);
    this.quantity = 10;
    this.notes = '';
    this.errorMessage = '';
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  getSelectedProduct(): Product | undefined {
    return this.products.find((p) => p.id === this.selectedProductId);
  }

  getPreviewStock(): number {
    const prod = this.getSelectedProduct();
    if (!prod) return 0;
    const current = prod.currentStock;
    return this.selectedType === 'IN' ? current + this.quantity : current - this.quantity;
  }

  onSubmit(): void {
    if (!this.selectedProductId) {
      this.errorMessage = 'Please select a product.';
      return;
    }
    if (!this.quantity || this.quantity <= 0) {
      this.errorMessage = 'Quantity must be at least 1 unit.';
      return;
    }

    const prod = this.getSelectedProduct();
    if (this.selectedType === 'OUT' && prod && prod.currentStock < this.quantity) {
      this.errorMessage = `Cannot deduct ${this.quantity} units. Available stock is only ${prod.currentStock}.`;
      return;
    }

    this.submitting = true;
    this.errorMessage = '';

    const currentUser = this.authService.getCurrentUser();

    const payload = {
      productId: this.selectedProductId,
      transactionType: this.selectedType,
      quantity: this.quantity,
      notes: this.notes.trim() || undefined,
      userId: currentUser?.id,
      userName: currentUser?.fullName || 'Staff'
    };

    this.transactionService.recordTransaction(payload).subscribe({
      next: () => {
        this.submitting = false;
        this.showModal = false;
        this.successMessage = `Recorded stock ${this.selectedType} of ${this.quantity} units successfully!`;
        this.loadTransactions();
        this.loadProducts(); // Refresh current stock in local list
        setTimeout(() => (this.successMessage = ''), 3000);
      },
      error: (err) => {
        this.submitting = false;
        this.errorMessage = err.error?.message || 'Transaction failed.';
      }
    });
  }
}
