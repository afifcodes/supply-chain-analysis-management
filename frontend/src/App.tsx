import { FormEvent, useCallback, useEffect, useMemo, useState } from 'react';
import {
  BarChart3,
  Boxes,
  ClipboardList,
  LayoutDashboard,
  PackagePlus,
  RefreshCw,
  Save,
  Search,
  Send,
  Truck,
  Users
} from 'lucide-react';
import { api } from './api';
import type { DashboardSummary, Order, OrderStatus, Product, ReportsSummary, Supplier } from './types';

type Page = 'dashboard' | 'products' | 'suppliers' | 'orders' | 'deliveries' | 'reports';

type ProductForm = {
  id?: number;
  sku: string;
  name: string;
  description: string;
  category: string;
  unitPrice: string;
  stockQuantity: string;
  lowStockThreshold: string;
  supplierId: string;
};

type SupplierForm = {
  id?: number;
  name: string;
  contactPerson: string;
  email: string;
  phone: string;
  address: string;
  notes: string;
};

type OrderForm = {
  productId: string;
  quantity: string;
  customerName: string;
  customerContact: string;
  expectedDeliveryDate: string;
};

const emptyProduct: ProductForm = {
  sku: '',
  name: '',
  description: '',
  category: '',
  unitPrice: '',
  stockQuantity: '',
  lowStockThreshold: '10',
  supplierId: ''
};

const emptySupplier: SupplierForm = {
  name: '',
  contactPerson: '',
  email: '',
  phone: '',
  address: '',
  notes: ''
};

const emptyOrder: OrderForm = {
  productId: '',
  quantity: '1',
  customerName: '',
  customerContact: '',
  expectedDeliveryDate: ''
};

const navItems: { page: Page; label: string; icon: typeof LayoutDashboard }[] = [
  { page: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { page: 'products', label: 'Products', icon: Boxes },
  { page: 'suppliers', label: 'Suppliers', icon: Users },
  { page: 'orders', label: 'Orders', icon: ClipboardList },
  { page: 'deliveries', label: 'Deliveries', icon: Truck },
  { page: 'reports', label: 'Reports', icon: BarChart3 }
];

const statusFlow: Record<OrderStatus, OrderStatus[]> = {
  PENDING: ['SHIPPED', 'CANCELLED'],
  SHIPPED: ['DELIVERED'],
  DELIVERED: [],
  CANCELLED: []
};

export default function App() {
  const [page, setPage] = useState<Page>('dashboard');
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [reports, setReports] = useState<ReportsSummary | null>(null);
  const [recentOrders, setRecentOrders] = useState<Order[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [orders, setOrders] = useState<Order[]>([]);
  const [deliveries, setDeliveries] = useState<Order[]>([]);
  const [productSearch, setProductSearch] = useState('');
  const [supplierSearch, setSupplierSearch] = useState('');
  const [orderStatus, setOrderStatus] = useState('');
  const [productForm, setProductForm] = useState<ProductForm>(emptyProduct);
  const [supplierForm, setSupplierForm] = useState<SupplierForm>(emptySupplier);
  const [orderForm, setOrderForm] = useState<OrderForm>(emptyOrder);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const showSuccess = (text: string) => {
    setMessage(text);
    setError('');
  };

  const showError = (err: unknown) => {
    setError(err instanceof Error ? err.message : 'Something went wrong.');
    setMessage('');
  };

  const loadAll = useCallback(async () => {
    setLoading(true);
    try {
      const [summaryData, recentData, productData, supplierData, orderData, deliveryData, reportData] = await Promise.all([
        api<DashboardSummary>('/dashboard/summary'),
        api<Order[]>('/dashboard/recent-orders?limit=5'),
        api<Product[]>(`/products${productSearch ? `?search=${encodeURIComponent(productSearch)}` : ''}`),
        api<Supplier[]>(`/suppliers${supplierSearch ? `?search=${encodeURIComponent(supplierSearch)}` : ''}`),
        api<Order[]>(`/orders${orderStatus ? `?status=${orderStatus}` : ''}`),
        api<Order[]>('/deliveries'),
        api<ReportsSummary>('/reports/summary')
      ]);
      setSummary(summaryData);
      setRecentOrders(recentData);
      setProducts(productData);
      setSuppliers(supplierData);
      setOrders(orderData);
      setDeliveries(deliveryData);
      setReports(reportData);
    } catch (err) {
      showError(err);
    } finally {
      setLoading(false);
    }
  }, [orderStatus, productSearch, supplierSearch]);

  useEffect(() => {
    void loadAll();
  }, [loadAll]);

  const pageTitle = useMemo(() => navItems.find((item) => item.page === page)?.label ?? 'Dashboard', [page]);

  async function saveSupplier(event: FormEvent) {
    event.preventDefault();
    try {
      const payload = {
        name: supplierForm.name,
        contactPerson: supplierForm.contactPerson || null,
        email: supplierForm.email || null,
        phone: supplierForm.phone || null,
        address: supplierForm.address || null,
        notes: supplierForm.notes || null
      };
      if (supplierForm.id) {
        await api(`/suppliers/${supplierForm.id}`, { method: 'PUT', body: JSON.stringify(payload) });
        showSuccess('Supplier updated.');
      } else {
        await api('/suppliers', { method: 'POST', body: JSON.stringify(payload) });
        showSuccess('Supplier added.');
      }
      setSupplierForm(emptySupplier);
      await loadAll();
    } catch (err) {
      showError(err);
    }
  }

  async function saveProduct(event: FormEvent) {
    event.preventDefault();
    try {
      const payload = {
        sku: productForm.sku,
        name: productForm.name,
        description: productForm.description || null,
        category: productForm.category || null,
        unitPrice: Number(productForm.unitPrice),
        stockQuantity: Number(productForm.stockQuantity),
        lowStockThreshold: Number(productForm.lowStockThreshold),
        supplierId: productForm.supplierId ? Number(productForm.supplierId) : null
      };
      if (productForm.id) {
        await api(`/products/${productForm.id}`, { method: 'PUT', body: JSON.stringify(payload) });
        showSuccess('Product updated.');
      } else {
        await api('/products', { method: 'POST', body: JSON.stringify(payload) });
        showSuccess('Product added.');
      }
      setProductForm(emptyProduct);
      await loadAll();
    } catch (err) {
      showError(err);
    }
  }

  async function createOrder(event: FormEvent) {
    event.preventDefault();
    try {
      await api('/orders', {
        method: 'POST',
        body: JSON.stringify({
          productId: Number(orderForm.productId),
          quantity: Number(orderForm.quantity),
          customerName: orderForm.customerName || null,
          customerContact: orderForm.customerContact || null,
          expectedDeliveryDate: orderForm.expectedDeliveryDate || null
        })
      });
      setOrderForm(emptyOrder);
      showSuccess('Order created and stock updated.');
      await loadAll();
    } catch (err) {
      showError(err);
    }
  }

  async function updateOrderStatus(order: Order, status: OrderStatus) {
    try {
      await api(`/orders/${order.id}/status`, {
        method: 'PATCH',
        body: JSON.stringify({ status })
      });
      showSuccess(`Order marked ${status.toLowerCase()}.`);
      await loadAll();
    } catch (err) {
      showError(err);
    }
  }

  async function deleteProduct(product: Product) {
    if (!window.confirm(`Delete ${product.name}? Products with order history are safely deactivated.`)) return;
    try {
      await api(`/products/${product.id}`, { method: 'DELETE' });
      showSuccess('Product removed.');
      await loadAll();
    } catch (err) {
      showError(err);
    }
  }

  async function deleteSupplier(supplier: Supplier) {
    if (!window.confirm(`Delete ${supplier.name}?`)) return;
    try {
      await api(`/suppliers/${supplier.id}`, { method: 'DELETE' });
      showSuccess('Supplier removed.');
      await loadAll();
    } catch (err) {
      showError(err);
    }
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <PackagePlus size={26} />
          <div>
            <strong>SCMS</strong>
            <span>College Mini Project</span>
          </div>
        </div>
        <nav>
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <button key={item.page} className={page === item.page ? 'active' : ''} onClick={() => setPage(item.page)}>
                <Icon size={18} />
                {item.label}
              </button>
            );
          })}
        </nav>
      </aside>

      <main className="content">
        <header className="topbar">
          <div>
            <h1>{pageTitle}</h1>
            <p>Local demo dashboard connected to Spring Boot APIs</p>
          </div>
          <button className="icon-button" onClick={() => void loadAll()} title="Refresh data">
            <RefreshCw size={18} />
          </button>
        </header>

        {message && <div className="notice success">{message}</div>}
        {error && <div className="notice error">{error}</div>}
        {loading && <div className="notice">Loading latest data...</div>}

        {page === 'dashboard' && <Dashboard summary={summary} recentOrders={recentOrders} />}
        {page === 'products' && (
          <Products
            products={products}
            suppliers={suppliers}
            search={productSearch}
            setSearch={setProductSearch}
            form={productForm}
            setForm={setProductForm}
            onSubmit={saveProduct}
            onDelete={deleteProduct}
          />
        )}
        {page === 'suppliers' && (
          <Suppliers
            suppliers={suppliers}
            search={supplierSearch}
            setSearch={setSupplierSearch}
            form={supplierForm}
            setForm={setSupplierForm}
            onSubmit={saveSupplier}
            onDelete={deleteSupplier}
          />
        )}
        {page === 'orders' && (
          <Orders
            products={products}
            orders={orders}
            status={orderStatus}
            setStatus={setOrderStatus}
            form={orderForm}
            setForm={setOrderForm}
            onSubmit={createOrder}
            onStatus={updateOrderStatus}
          />
        )}
        {page === 'deliveries' && <Deliveries deliveries={deliveries} />}
        {page === 'reports' && <Reports reports={reports} />}
      </main>
    </div>
  );
}

function Dashboard({ summary, recentOrders }: { summary: DashboardSummary | null; recentOrders: Order[] }) {
  const cards = [
    ['Products', summary?.totalProducts ?? 0],
    ['Suppliers', summary?.totalSuppliers ?? 0],
    ['Orders', summary?.totalOrders ?? 0],
    ['Pending', summary?.pendingOrders ?? 0],
    ['Low Stock', summary?.lowStockProducts ?? 0],
    ['Delayed', summary?.delayedDeliveries ?? 0]
  ];
  return (
    <section>
      <div className="metrics-grid">
        {cards.map(([label, value]) => (
          <article className="metric-card" key={label}>
            <span>{label}</span>
            <strong>{value}</strong>
          </article>
        ))}
      </div>
      <div className="panel">
        <h2>Recent Orders</h2>
        <OrderTable orders={recentOrders} compact />
      </div>
    </section>
  );
}

function Products(props: {
  products: Product[];
  suppliers: Supplier[];
  search: string;
  setSearch: (value: string) => void;
  form: ProductForm;
  setForm: (value: ProductForm) => void;
  onSubmit: (event: FormEvent) => void;
  onDelete: (product: Product) => void;
}) {
  return (
    <section className="two-column">
      <div className="panel">
        <div className="panel-header">
          <h2>Product Inventory</h2>
          <label className="search">
            <Search size={16} />
            <input value={props.search} onChange={(event) => props.setSearch(event.target.value)} placeholder="Search SKU or name" />
          </label>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>SKU</th>
                <th>Name</th>
                <th>Supplier</th>
                <th>Price</th>
                <th>Stock</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {props.products.map((product) => (
                <tr key={product.id}>
                  <td>{product.sku}</td>
                  <td>{product.name}</td>
                  <td>{product.supplierName ?? '-'}</td>
                  <td>{money(product.unitPrice)}</td>
                  <td>
                    <span className={product.lowStock ? 'badge danger' : 'badge ok'}>{product.stockQuantity}</span>
                  </td>
                  <td className="actions">
                    <button onClick={() => props.setForm(productToForm(product))}>Edit</button>
                    <button className="danger-text" onClick={() => props.onDelete(product)}>Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {props.products.length === 0 && <EmptyState text="No products found." />}
        </div>
      </div>
      <form className="panel form-panel" onSubmit={props.onSubmit}>
        <h2>{props.form.id ? 'Edit Product' : 'Add Product'}</h2>
        <input required placeholder="SKU" value={props.form.sku} onChange={(event) => props.setForm({ ...props.form, sku: event.target.value })} />
        <input required placeholder="Product name" value={props.form.name} onChange={(event) => props.setForm({ ...props.form, name: event.target.value })} />
        <input placeholder="Category" value={props.form.category} onChange={(event) => props.setForm({ ...props.form, category: event.target.value })} />
        <textarea placeholder="Description" value={props.form.description} onChange={(event) => props.setForm({ ...props.form, description: event.target.value })} />
        <div className="form-grid">
          <input required type="number" min="0" step="0.01" placeholder="Unit price" value={props.form.unitPrice} onChange={(event) => props.setForm({ ...props.form, unitPrice: event.target.value })} />
          <input required type="number" min="0" placeholder="Stock" value={props.form.stockQuantity} onChange={(event) => props.setForm({ ...props.form, stockQuantity: event.target.value })} />
        </div>
        <input required type="number" min="0" placeholder="Low-stock threshold" value={props.form.lowStockThreshold} onChange={(event) => props.setForm({ ...props.form, lowStockThreshold: event.target.value })} />
        <select value={props.form.supplierId} onChange={(event) => props.setForm({ ...props.form, supplierId: event.target.value })}>
          <option value="">No supplier</option>
          {props.suppliers.map((supplier) => <option key={supplier.id} value={supplier.id}>{supplier.name}</option>)}
        </select>
        <div className="form-actions">
          <button type="submit"><Save size={16} /> Save</button>
          <button type="button" className="secondary" onClick={() => props.setForm(emptyProduct)}>Cancel</button>
        </div>
      </form>
    </section>
  );
}

function Suppliers(props: {
  suppliers: Supplier[];
  search: string;
  setSearch: (value: string) => void;
  form: SupplierForm;
  setForm: (value: SupplierForm) => void;
  onSubmit: (event: FormEvent) => void;
  onDelete: (supplier: Supplier) => void;
}) {
  return (
    <section className="two-column">
      <div className="panel">
        <div className="panel-header">
          <h2>Suppliers</h2>
          <label className="search">
            <Search size={16} />
            <input value={props.search} onChange={(event) => props.setSearch(event.target.value)} placeholder="Search suppliers" />
          </label>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Contact</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {props.suppliers.map((supplier) => (
                <tr key={supplier.id}>
                  <td>{supplier.name}</td>
                  <td>{supplier.contactPerson ?? '-'}</td>
                  <td>{supplier.email ?? '-'}</td>
                  <td>{supplier.phone ?? '-'}</td>
                  <td className="actions">
                    <button onClick={() => props.setForm(supplierToForm(supplier))}>Edit</button>
                    <button className="danger-text" onClick={() => props.onDelete(supplier)}>Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {props.suppliers.length === 0 && <EmptyState text="No suppliers found." />}
        </div>
      </div>
      <form className="panel form-panel" onSubmit={props.onSubmit}>
        <h2>{props.form.id ? 'Edit Supplier' : 'Add Supplier'}</h2>
        <input required placeholder="Company name" value={props.form.name} onChange={(event) => props.setForm({ ...props.form, name: event.target.value })} />
        <input placeholder="Contact person" value={props.form.contactPerson} onChange={(event) => props.setForm({ ...props.form, contactPerson: event.target.value })} />
        <input type="email" placeholder="Email" value={props.form.email} onChange={(event) => props.setForm({ ...props.form, email: event.target.value })} />
        <input placeholder="Phone" value={props.form.phone} onChange={(event) => props.setForm({ ...props.form, phone: event.target.value })} />
        <textarea placeholder="Address" value={props.form.address} onChange={(event) => props.setForm({ ...props.form, address: event.target.value })} />
        <textarea placeholder="Notes" value={props.form.notes} onChange={(event) => props.setForm({ ...props.form, notes: event.target.value })} />
        <div className="form-actions">
          <button type="submit"><Save size={16} /> Save</button>
          <button type="button" className="secondary" onClick={() => props.setForm(emptySupplier)}>Cancel</button>
        </div>
      </form>
    </section>
  );
}

function Orders(props: {
  products: Product[];
  orders: Order[];
  status: string;
  setStatus: (value: string) => void;
  form: OrderForm;
  setForm: (value: OrderForm) => void;
  onSubmit: (event: FormEvent) => void;
  onStatus: (order: Order, status: OrderStatus) => void;
}) {
  return (
    <section className="two-column">
      <div className="panel">
        <div className="panel-header">
          <h2>Orders</h2>
          <select value={props.status} onChange={(event) => props.setStatus(event.target.value)}>
            <option value="">All statuses</option>
            {(['PENDING', 'SHIPPED', 'DELIVERED', 'CANCELLED'] as OrderStatus[]).map((status) => <option key={status} value={status}>{status}</option>)}
          </select>
        </div>
        <OrderTable orders={props.orders} onStatus={props.onStatus} />
      </div>
      <form className="panel form-panel" onSubmit={props.onSubmit}>
        <h2>Create Order</h2>
        <select required value={props.form.productId} onChange={(event) => props.setForm({ ...props.form, productId: event.target.value })}>
          <option value="">Select product</option>
          {props.products.map((product) => (
            <option key={product.id} value={product.id}>{product.name} ({product.stockQuantity} in stock)</option>
          ))}
        </select>
        <input required type="number" min="1" placeholder="Quantity" value={props.form.quantity} onChange={(event) => props.setForm({ ...props.form, quantity: event.target.value })} />
        <input placeholder="Customer name" value={props.form.customerName} onChange={(event) => props.setForm({ ...props.form, customerName: event.target.value })} />
        <input placeholder="Customer contact" value={props.form.customerContact} onChange={(event) => props.setForm({ ...props.form, customerContact: event.target.value })} />
        <label>
          Expected delivery
          <input type="date" value={props.form.expectedDeliveryDate} onChange={(event) => props.setForm({ ...props.form, expectedDeliveryDate: event.target.value })} />
        </label>
        <button type="submit"><Send size={16} /> Create Order</button>
      </form>
    </section>
  );
}

function Deliveries({ deliveries }: { deliveries: Order[] }) {
  return (
    <section className="panel">
      <h2>Delivery Tracking</h2>
      <OrderTable orders={deliveries} compact />
    </section>
  );
}

function Reports({ reports }: { reports: ReportsSummary | null }) {
  const statuses: OrderStatus[] = ['PENDING', 'SHIPPED', 'DELIVERED', 'CANCELLED'];
  const max = Math.max(...statuses.map((status) => reports?.orderCountByStatus?.[status] ?? 0), 1);
  const analytics = reports?.analytics;
  const maxCategoryRevenue = Math.max(...(analytics?.categoryPerformance ?? []).map((category) => category.revenue), 1);
  const maxMonthlyRevenue = Math.max(...(analytics?.monthlyRevenue ?? []).map((month) => month.revenue), 1);
  return (
    <section className="reports-grid">
      <div className="panel">
        <h2>Order Status</h2>
        {statuses.map((status) => {
          const value = reports?.orderCountByStatus?.[status] ?? 0;
          return (
            <div className="bar-row" key={status}>
              <span>{status}</span>
              <div><i style={{ width: `${(value / max) * 100}%` }} /></div>
              <strong>{value}</strong>
            </div>
          );
        })}
      </div>
      <div className="panel">
        <h2>Delivery Performance</h2>
        <div className="metrics-grid small">
          <article className="metric-card"><span>On time</span><strong>{reports?.deliveredOnTime ?? 0}</strong></article>
          <article className="metric-card"><span>Delayed</span><strong>{reports?.delayedDeliveries ?? 0}</strong></article>
          <article className="metric-card"><span>Order value</span><strong>{money(reports?.totalOrderValue ?? 0)}</strong></article>
        </div>
      </div>
      <div className="panel wide">
        <h2>EDA Snapshot</h2>
        <div className="metrics-grid analytics">
          <article className="metric-card"><span>Inventory value</span><strong>{money(analytics?.inventoryValue ?? 0)}</strong></article>
          <article className="metric-card"><span>Avg order value</span><strong>{money(analytics?.averageOrderValue ?? 0)}</strong></article>
          <article className="metric-card"><span>Avg qty/order</span><strong>{numberValue(analytics?.averageOrderQuantity ?? 0)}</strong></article>
          <article className="metric-card"><span>Units sold</span><strong>{analytics?.totalUnitsSold ?? 0}</strong></article>
          <article className="metric-card"><span>Stock turnover</span><strong>{numberValue(analytics?.stockTurnoverRatio ?? 0)}x</strong></article>
          <article className="metric-card"><span>No supplier</span><strong>{analytics?.unsuppliedProducts ?? 0}</strong></article>
        </div>
      </div>
      <div className="panel">
        <h2>Top Products By Revenue</h2>
        <div className="table-wrap">
          <table>
            <thead><tr><th>Product</th><th>SKU</th><th>Units</th><th>Revenue</th><th>Stock</th></tr></thead>
            <tbody>
              {(analytics?.topProducts ?? []).map((product) => (
                <tr key={product.productId}>
                  <td>{product.name}</td>
                  <td>{product.sku}</td>
                  <td>{product.unitsSold}</td>
                  <td>{money(product.revenue)}</td>
                  <td>{product.currentStock}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {(analytics?.topProducts ?? []).length === 0 && <EmptyState text="No completed sales data yet." />}
        </div>
      </div>
      <div className="panel">
        <h2>Business Insights</h2>
        <ul className="insight-list">
          {(analytics?.businessInsights ?? ['No analytics available yet.']).map((insight) => (
            <li key={insight}>{insight}</li>
          ))}
        </ul>
      </div>
      <div className="panel">
        <h2>Category Analysis</h2>
        {(analytics?.categoryPerformance ?? []).map((category) => (
          <div className="analysis-row" key={category.category}>
            <div>
              <strong>{category.category}</strong>
              <span>{category.productCount} products · {category.unitsSold} units sold · stock {money(category.stockValue)}</span>
            </div>
            <div className="analysis-bar"><i style={{ width: `${(category.revenue / maxCategoryRevenue) * 100}%` }} /></div>
            <b>{money(category.revenue)}</b>
          </div>
        ))}
        {(analytics?.categoryPerformance ?? []).length === 0 && <EmptyState text="No category data yet." />}
      </div>
      <div className="panel">
        <h2>Monthly Revenue Trend</h2>
        {(analytics?.monthlyRevenue ?? []).map((month) => (
          <div className="analysis-row" key={month.month}>
            <div>
              <strong>{month.month}</strong>
              <span>{month.orderCount} orders</span>
            </div>
            <div className="analysis-bar"><i style={{ width: `${(month.revenue / maxMonthlyRevenue) * 100}%` }} /></div>
            <b>{money(month.revenue)}</b>
          </div>
        ))}
        {(analytics?.monthlyRevenue ?? []).length === 0 && <EmptyState text="No monthly revenue yet." />}
      </div>
      <div className="panel wide">
        <h2>Supplier Stock Exposure</h2>
        <div className="table-wrap">
          <table>
            <thead><tr><th>Supplier</th><th>Products</th><th>Stock Value</th></tr></thead>
            <tbody>
              {(analytics?.supplierPerformance ?? []).map((supplier) => (
                <tr key={supplier.supplier}>
                  <td>{supplier.supplier}</td>
                  <td>{supplier.productCount}</td>
                  <td>{money(supplier.stockValue)}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {(analytics?.supplierPerformance ?? []).length === 0 && <EmptyState text="No supplier stock data yet." />}
        </div>
      </div>
      <div className="panel wide">
        <h2>Low Stock Products</h2>
        <div className="table-wrap">
          <table>
            <thead><tr><th>SKU</th><th>Name</th><th>Stock</th><th>Threshold</th></tr></thead>
            <tbody>
              {(reports?.lowStockProducts ?? []).map((product) => (
                <tr key={product.id}>
                  <td>{product.sku}</td>
                  <td>{product.name}</td>
                  <td>{product.stockQuantity}</td>
                  <td>{product.lowStockThreshold}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {(reports?.lowStockProducts ?? []).length === 0 && <EmptyState text="No low-stock products." />}
        </div>
      </div>
    </section>
  );
}

function OrderTable({ orders, compact, onStatus }: { orders: Order[]; compact?: boolean; onStatus?: (order: Order, status: OrderStatus) => void }) {
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Order</th>
            <th>Product</th>
            <th>Qty</th>
            <th>Total</th>
            <th>Status</th>
            <th>Expected</th>
            <th>Delay</th>
            {!compact && <th>Actions</th>}
          </tr>
        </thead>
        <tbody>
          {orders.map((order) => (
            <tr key={order.id}>
              <td>{order.orderNumber}</td>
              <td>{order.productName}</td>
              <td>{order.quantity}</td>
              <td>{money(order.totalAmount)}</td>
              <td><span className={`badge ${order.status.toLowerCase()}`}>{order.status}</span></td>
              <td>{order.expectedDeliveryDate ?? '-'}</td>
              <td>{order.delayed ? `${order.delayDays} day(s)` : '-'}</td>
              {!compact && (
                <td className="actions">
                  {statusFlow[order.status].map((status) => (
                    <button key={status} onClick={() => onStatus?.(order, status)}>{status}</button>
                  ))}
                  {statusFlow[order.status].length === 0 && <span className="muted">Final</span>}
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
      {orders.length === 0 && <EmptyState text="No orders to show." />}
    </div>
  );
}

function EmptyState({ text }: { text: string }) {
  return <div className="empty">{text}</div>;
}

function productToForm(product: Product): ProductForm {
  return {
    id: product.id,
    sku: product.sku,
    name: product.name,
    description: product.description ?? '',
    category: product.category ?? '',
    unitPrice: String(product.unitPrice),
    stockQuantity: String(product.stockQuantity),
    lowStockThreshold: String(product.lowStockThreshold),
    supplierId: product.supplierId ? String(product.supplierId) : ''
  };
}

function supplierToForm(supplier: Supplier): SupplierForm {
  return {
    id: supplier.id,
    name: supplier.name,
    contactPerson: supplier.contactPerson ?? '',
    email: supplier.email ?? '',
    phone: supplier.phone ?? '',
    address: supplier.address ?? '',
    notes: supplier.notes ?? ''
  };
}

function money(value: number) {
  return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(value);
}

function numberValue(value: number) {
  return new Intl.NumberFormat('en-IN', { maximumFractionDigits: 2 }).format(value);
}
