// ==============================================================
// SmartShelfX - TypeScript Core Data Models
// Matches Spring Boot REST API DTOs exactly
// ==============================================================

export type UserRole = 'ADMIN' | 'WAREHOUSE_MANAGER' | 'VENDOR';

export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  role: UserRole;
  enabled: boolean;
  createdAt: string;
}

export interface JwtResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  email: string;
  fullName: string;
  role: UserRole;
}

export interface Category {
  id: number;
  name: string;
  description?: string;
  createdAt?: string;
  productCount?: number;
}

export interface CategoryRequest {
  name: string;
  description?: string;
}

export interface Product {
  id: number;
  name: string;
  sku: string;
  description?: string;
  categoryId: number;
  categoryName?: string;
  vendorId?: number;
  vendorName?: string;
  price: number;
  costPrice: number;
  currentStock: number;
  minStockThreshold: number;
  reorderQuantity: number;
  unit: string;
  stockStatus: 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK';
  totalInventoryValue: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProductRequest {
  name: string;
  sku: string;
  description?: string;
  categoryId: number;
  vendorId?: number;
  vendorName?: string;
  price: number;
  costPrice: number;
  currentStock: number;
  minStockThreshold: number;
  reorderQuantity: number;
  unit: string;
}

export interface StockTransaction {
  id: number;
  productId: number;
  productName?: string;
  productSku?: string;
  categoryName?: string;
  transactionType: 'IN' | 'OUT';
  quantity: number;
  previousStock: number;
  newStock: number;
  notes?: string;
  userId?: number;
  userName?: string;
  transactionDate: string;
}

export interface StockTransactionRequest {
  productId: number;
  transactionType: 'IN' | 'OUT';
  quantity: number;
  notes?: string;
  userId?: number;
  userName?: string;
}

export interface PurchaseOrderItem {
  id?: number;
  productId: number;
  productName?: string;
  productSku?: string;
  unit?: string;
  quantityOrdered: number;
  unitCost: number;
  totalCost?: number;
}

export interface PurchaseOrder {
  id: number;
  orderNumber: string;
  vendorId?: number;
  vendorName?: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'RECEIVED';
  totalAmount: number;
  notes?: string;
  createdBy?: number;
  createdByName?: string;
  createdAt: string;
  updatedAt?: string;
  receivedAt?: string;
  items: PurchaseOrderItem[];
}

export interface PurchaseOrderRequest {
  vendorId?: number;
  vendorName?: string;
  notes?: string;
  createdBy?: number;
  createdByName?: string;
  items: {
    productId: number;
    quantityOrdered: number;
    unitCost: number;
  }[];
}

export interface OrderStatusUpdateRequest {
  status: 'APPROVED' | 'REJECTED' | 'RECEIVED';
  notes?: string;
  userId?: number;
  userName?: string;
}

export interface RestockSuggestion {
  productId: number;
  productName: string;
  productSku: string;
  categoryName?: string;
  vendorId?: number;
  vendorName?: string;
  currentStock: number;
  minStockThreshold: number;
  reorderQuantity: number;
  suggestedOrderQuantity: number;
  unitCost: number;
  estimatedTotalCost: number;
  priority: 'HIGH' | 'MEDIUM';
  reason: string;
}

export interface NotificationItem {
  id: number;
  title: string;
  message: string;
  type: 'LOW_STOCK' | 'OUT_OF_STOCK' | 'ORDER_UPDATE' | 'SYSTEM';
  read: boolean;
  productId?: number;
  createdAt: string;
}

export interface TrendDataPoint {
  date: string;
  stockIn: number;
  stockOut: number;
}

export interface DashboardStats {
  totalProducts: number;
  totalInventoryValue: number;
  lowStockCount: number;
  outOfStockCount: number;
  totalCategories: number;
  pendingOrdersCount: number;
  recentTransactions: StockTransaction[];
  lowStockProducts: Product[];
  categoryProductCount: { [key: string]: number };
  movementTrends: TrendDataPoint[];
}

export interface ForecastResponse {
  productId: number;
  productName: string;
  productSku: string;
  forecastPeriodDays: number;
  totalPredictedDemand: number;
  dailyForecasts: number[];
  historicalDataPoints: number;
  modelEvaluation?: {
    modelType: string;
    meanAbsoluteError?: number;
    rootMeanSquaredError?: number;
    baselineMovingAverageMae?: number;
    modelAccuracyNote?: string;
  };
  restockingRecommendation?: {
    leadTimeDays: number;
    predictedDemandDuringLeadTime: number;
    safetyStockBuffer: number;
    currentWarehouseStock: number;
    stockAlreadyOnOrder?: number;
    recommendedOrderQuantity: number;
    restockUrgency: string;
    explanation: string;
  };
  limitationsNote?: string;
  sourceService?: string;
}
