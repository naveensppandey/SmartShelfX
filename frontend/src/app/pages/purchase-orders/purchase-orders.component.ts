import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../../core/services/order.service';
import { ProductService } from '../../core/services/product.service';
import { AuthService } from '../../core/services/auth.service';
import { Product, PurchaseOrder, PurchaseOrderRequest, RestockSuggestion } from '../../core/models/models';

@Component({
  selector: 'app-purchase-orders',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './purchase-orders.component.html',
  styleUrls: ['./purchase-orders.component.scss']
})
export class PurchaseOrdersComponent implements OnInit {
  activeTab: 'orders' | 'suggestions' = 'orders';

  // Orders list & filter
  orders: PurchaseOrder[] = [];
  filteredOrders: PurchaseOrder[] = [];
  statusFilter: string = 'ALL';
  searchTerm: string = '';
  loadingOrders: boolean = false;
  orderError: string | null = null;
  orderSuccess: string | null = null;

  // Restock suggestions
  suggestions: RestockSuggestion[] = [];
  loadingSuggestions: boolean = false;

  // Selected Order for Detail Modal
  selectedOrder: PurchaseOrder | null = null;
  showDetailModal: boolean = false;
  updatingStatus: boolean = false;

  // Create Order Modal
  showCreateModal: boolean = false;
  availableProducts: Product[] = [];
  newOrderVendorName: string = '';
  newOrderNotes: string = '';
  newOrderItems: { productId: number; quantityOrdered: number; unitCost: number; productName?: string }[] = [];
  isCreating: boolean = false;

  // User role
  userRole: string | null = null;

  constructor(
    private orderService: OrderService,
    private productService: ProductService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.userRole = this.authService.getUserRole();
    this.loadOrders();
    this.loadSuggestions();
    this.loadProductsForDropdown();
  }

  loadOrders(): void {
    this.loadingOrders = true;
    this.orderError = null;
    this.orderService.getPurchaseOrders().subscribe({
      next: (data: PurchaseOrder[]) => {
        this.orders = data;
        this.applyFilters();
        this.loadingOrders = false;
      },
      error: (err: any) => {
        this.orderError = 'Failed to load purchase orders: ' + (err.error?.message || err.message);
        this.loadingOrders = false;
      }
    });
  }

  loadSuggestions(): void {
    this.loadingSuggestions = true;
    this.orderService.getRestockSuggestions().subscribe({
      next: (data: RestockSuggestion[]) => {
        this.suggestions = data;
        this.loadingSuggestions = false;
      },
      error: () => {
        this.loadingSuggestions = false;
      }
    });
  }

  loadProductsForDropdown(): void {
    this.productService.getAllProducts().subscribe({
      next: (prods: Product[]) => {
        this.availableProducts = prods;
      }
    });
  }

  applyFilters(): void {
    const term = this.searchTerm.trim().toLowerCase();
    this.filteredOrders = this.orders.filter((order) => {
      const matchesStatus = this.statusFilter === 'ALL' || order.status === this.statusFilter;
      const matchesSearch =
        !term ||
        order.orderNumber.toLowerCase().includes(term) ||
        (order.vendorName && order.vendorName.toLowerCase().includes(term)) ||
        (order.notes && order.notes.toLowerCase().includes(term));
      return matchesStatus && matchesSearch;
    });
  }

  viewOrderDetails(order: PurchaseOrder): void {
    this.selectedOrder = order;
    this.showDetailModal = true;
  }

  closeDetailModal(): void {
    this.selectedOrder = null;
    this.showDetailModal = false;
  }

  updateStatus(newStatus: 'APPROVED' | 'REJECTED' | 'RECEIVED'): void {
    if (!this.selectedOrder) return;

    if (newStatus === 'RECEIVED') {
      const confirmed = confirm(
        'Receiving this order will automatically increase inventory stock for all items and log audit movements. This action cannot be undone. Proceed?'
      );
      if (!confirmed) return;
    }

    this.updatingStatus = true;
    const currentUser = this.authService.getCurrentUser();

    this.orderService.updateOrderStatus(this.selectedOrder.id, {
      status: newStatus,
      userId: currentUser?.id,
      userName: currentUser?.fullName || currentUser?.username
    }).subscribe({
      next: (updated) => {
        this.orderSuccess = `Order ${updated.orderNumber} status changed to ${updated.status}.`;
        this.updatingStatus = false;
        this.closeDetailModal();
        this.loadOrders();
        this.loadSuggestions();
        setTimeout(() => (this.orderSuccess = null), 5000);
      },
      error: (err) => {
        this.orderError = 'Failed to update order status: ' + (err.error?.message || err.message);
        this.updatingStatus = false;
      }
    });
  }

  // Create Order Methods
  openCreateModal(prefillSuggestion?: RestockSuggestion): void {
    this.newOrderVendorName = prefillSuggestion?.vendorName || '';
    this.newOrderNotes = prefillSuggestion ? `Restock order triggered from inventory suggestion for ${prefillSuggestion.productName}` : '';
    this.newOrderItems = [];

    if (prefillSuggestion) {
      this.newOrderItems.push({
        productId: prefillSuggestion.productId,
        quantityOrdered: prefillSuggestion.suggestedOrderQuantity,
        unitCost: prefillSuggestion.unitCost,
        productName: prefillSuggestion.productName
      });
    } else if (this.availableProducts.length > 0) {
      this.addItemRow();
    }

    this.showCreateModal = true;
  }

  closeCreateModal(): void {
    this.showCreateModal = false;
    this.newOrderItems = [];
  }

  addItemRow(): void {
    if (this.availableProducts.length === 0) return;
    const first = this.availableProducts[0];
    this.newOrderItems.push({
      productId: first.id,
      quantityOrdered: 10,
      unitCost: first.costPrice || 10,
      productName: first.name
    });
  }

  removeItemRow(index: number): void {
    this.newOrderItems.splice(index, 1);
  }

  onProductSelect(item: { productId: number; quantityOrdered: number; unitCost: number; productName?: string }): void {
    const prod = this.availableProducts.find((p) => p.id === Number(item.productId));
    if (prod) {
      item.productName = prod.name;
      item.unitCost = prod.costPrice;
      if (!this.newOrderVendorName && prod.vendorName) {
        this.newOrderVendorName = prod.vendorName;
      }
    }
  }

  getNewOrderTotal(): number {
    return this.newOrderItems.reduce((acc, it) => acc + (it.quantityOrdered * it.unitCost), 0);
  }

  submitCreateOrder(): void {
    if (this.newOrderItems.length === 0) {
      alert('Please add at least one item to the order.');
      return;
    }
    for (const it of this.newOrderItems) {
      if (it.quantityOrdered <= 0) {
        alert('Quantity ordered must be greater than 0.');
        return;
      }
    }

    this.isCreating = true;
    const currentUser = this.authService.getCurrentUser();
    const req: PurchaseOrderRequest = {
      vendorName: this.newOrderVendorName || 'General Supplier',
      notes: this.newOrderNotes,
      createdBy: currentUser?.id,
      createdByName: currentUser?.fullName || currentUser?.username,
      items: this.newOrderItems.map((it) => ({
        productId: Number(it.productId),
        quantityOrdered: Number(it.quantityOrdered),
        unitCost: Number(it.unitCost)
      }))
    };

    this.orderService.createPurchaseOrder(req).subscribe({
      next: (po) => {
        this.orderSuccess = `Purchase Order ${po.orderNumber} created successfully!`;
        this.isCreating = false;
        this.closeCreateModal();
        this.activeTab = 'orders';
        this.loadOrders();
        setTimeout(() => (this.orderSuccess = null), 5000);
      },
      error: (err) => {
        this.orderError = 'Failed to create purchase order: ' + (err.error?.message || err.message);
        this.isCreating = false;
      }
    });
  }

  canManageOrders(): boolean {
    return this.userRole === 'ADMIN' || this.userRole === 'WAREHOUSE_MANAGER';
  }
}
