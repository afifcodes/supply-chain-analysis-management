export type OrderStatus = 'PENDING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';

export type Supplier = {
  id: number;
  name: string;
  contactPerson?: string | null;
  email?: string | null;
  phone?: string | null;
  address?: string | null;
  notes?: string | null;
};

export type Product = {
  id: number;
  sku: string;
  name: string;
  description?: string | null;
  category?: string | null;
  unitPrice: number;
  stockQuantity: number;
  lowStockThreshold: number;
  lowStock: boolean;
  supplierId?: number | null;
  supplierName?: string | null;
  active: boolean;
};

export type Order = {
  id: number;
  orderNumber: string;
  productId: number;
  productName: string;
  productSku: string;
  quantity: number;
  unitPrice: number;
  totalAmount: number;
  customerName?: string | null;
  customerContact?: string | null;
  status: OrderStatus;
  orderDate: string;
  expectedDeliveryDate?: string | null;
  actualDeliveryDate?: string | null;
  delayed: boolean;
  delayDays?: number | null;
};

export type DashboardSummary = {
  totalProducts: number;
  totalSuppliers: number;
  totalOrders: number;
  pendingOrders: number;
  lowStockProducts: number;
  deliveredOnTime: number;
  delayedDeliveries: number;
  totalOrderValue: number;
};

export type ReportsSummary = {
  orderCountByStatus: Record<OrderStatus, number>;
  lowStockProducts: Product[];
  deliveredOnTime: number;
  delayedDeliveries: number;
  totalOrderValue: number;
  analytics: AnalyticsSummary;
};

export type AnalyticsSummary = {
  inventoryValue: number;
  averageOrderValue: number;
  averageOrderQuantity: number;
  stockTurnoverRatio: number;
  totalUnitsSold: number;
  unsuppliedProducts: number;
  topProducts: ProductPerformance[];
  categoryPerformance: CategoryPerformance[];
  supplierPerformance: SupplierPerformance[];
  monthlyRevenue: MonthlyRevenue[];
  businessInsights: string[];
};

export type ProductPerformance = {
  productId: number;
  sku: string;
  name: string;
  unitsSold: number;
  revenue: number;
  currentStock: number;
};

export type CategoryPerformance = {
  category: string;
  productCount: number;
  unitsSold: number;
  revenue: number;
  stockValue: number;
};

export type SupplierPerformance = {
  supplier: string;
  productCount: number;
  stockValue: number;
};

export type MonthlyRevenue = {
  month: string;
  orderCount: number;
  revenue: number;
};

