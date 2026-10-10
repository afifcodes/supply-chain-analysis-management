package com.example.supplychain.service;

import com.example.supplychain.domain.CustomerOrder;
import com.example.supplychain.domain.OrderStatus;
import com.example.supplychain.domain.Product;
import com.example.supplychain.dto.DashboardSummaryResponse;
import com.example.supplychain.dto.ReportsSummaryResponse;
import com.example.supplychain.dto.ReportsSummaryResponse.AnalyticsSummary;
import com.example.supplychain.dto.ReportsSummaryResponse.CategoryPerformance;
import com.example.supplychain.dto.ReportsSummaryResponse.MonthlyRevenue;
import com.example.supplychain.dto.ReportsSummaryResponse.ProductPerformance;
import com.example.supplychain.dto.ReportsSummaryResponse.SupplierPerformance;
import com.example.supplychain.repository.CustomerOrderRepository;
import com.example.supplychain.repository.ProductRepository;
import com.example.supplychain.repository.SupplierRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private final ProductRepository products;
    private final SupplierRepository suppliers;
    private final CustomerOrderRepository orders;
    private final ProductService productService;
    private final DeliveryCalculator deliveryCalculator;

    public DashboardService(
            ProductRepository products,
            SupplierRepository suppliers,
            CustomerOrderRepository orders,
            ProductService productService,
            DeliveryCalculator deliveryCalculator) {
        this.products = products;
        this.suppliers = suppliers;
        this.orders = orders;
        this.productService = productService;
        this.deliveryCalculator = deliveryCalculator;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary() {
        DeliveryCounts deliveryCounts = deliveryCounts();
        return new DashboardSummaryResponse(
                products.countByActiveTrue(),
                suppliers.count(),
                orders.count(),
                orders.countByStatus(OrderStatus.PENDING),
                products.countLowStockProducts(),
                deliveryCounts.onTime(),
                deliveryCounts.delayed(),
                orders.sumTotalAmount());
    }

    @Transactional(readOnly = true)
    public ReportsSummaryResponse reports() {
        DeliveryCounts deliveryCounts = deliveryCounts();
        Map<String, Long> counts = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            counts.put(status.name(), orders.countByStatus(status));
        }
        return new ReportsSummaryResponse(
                counts,
                productService.list(null, true),
                deliveryCounts.onTime(),
                deliveryCounts.delayed(),
                orders.sumTotalAmount(),
                analytics());
    }

    private DeliveryCounts deliveryCounts() {
        long delayed = 0;
        long onTime = 0;
        for (CustomerOrder order : orders.findAll()) {
            if (order.getStatus() == OrderStatus.CANCELLED || order.getExpectedDeliveryDate() == null) {
                continue;
            }
            if (deliveryCalculator.isDelayed(order)) {
                delayed++;
            } else if (order.getStatus() == OrderStatus.DELIVERED) {
                onTime++;
            }
        }
        return new DeliveryCounts(onTime, delayed);
    }

    private AnalyticsSummary analytics() {
        List<Product> activeProducts = products.findByActiveTrueOrderByNameAsc();
        List<CustomerOrder> allOrders = orders.findAllByOrderByOrderDateDesc();

        BigDecimal inventoryValue = activeProducts.stream()
                .map(product -> product.getUnitPrice().multiply(BigDecimal.valueOf(product.getStockQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalValue = allOrders.stream()
                .map(CustomerOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long totalUnitsSold = allOrders.stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .mapToLong(CustomerOrder::getQuantity)
                .sum();
        long activeStock = activeProducts.stream()
                .mapToLong(Product::getStockQuantity)
                .sum();
        BigDecimal averageOrderValue = allOrders.isEmpty()
                ? BigDecimal.ZERO
                : totalValue.divide(BigDecimal.valueOf(allOrders.size()), 2, RoundingMode.HALF_UP);
        double averageOrderQuantity = allOrders.isEmpty()
                ? 0
                : round(totalUnitsSold / (double) allOrders.size());
        double stockTurnoverRatio = activeStock == 0
                ? 0
                : round(totalUnitsSold / (double) activeStock);
        long unsuppliedProducts = activeProducts.stream()
                .filter(product -> product.getSupplier() == null)
                .count();

        return new AnalyticsSummary(
                inventoryValue,
                averageOrderValue,
                averageOrderQuantity,
                stockTurnoverRatio,
                totalUnitsSold,
                unsuppliedProducts,
                topProducts(allOrders, activeProducts),
                categoryPerformance(allOrders, activeProducts),
                supplierPerformance(activeProducts),
                monthlyRevenue(allOrders),
                businessInsights(activeProducts, allOrders, inventoryValue, stockTurnoverRatio, unsuppliedProducts));
    }

    private List<ProductPerformance> topProducts(List<CustomerOrder> allOrders, List<Product> activeProducts) {
        Map<Long, ProductTotals> totals = new HashMap<>();
        for (CustomerOrder order : allOrders) {
            if (order.getStatus() == OrderStatus.CANCELLED) {
                continue;
            }
            Product product = order.getProduct();
            totals.computeIfAbsent(product.getId(), id -> new ProductTotals()).add(order);
        }
        Map<Long, Product> productsById = new HashMap<>();
        for (Product product : activeProducts) {
            productsById.put(product.getId(), product);
        }
        return totals.entrySet().stream()
                .sorted(Comparator.comparing((Map.Entry<Long, ProductTotals> entry) -> entry.getValue().revenue()).reversed())
                .limit(5)
                .map(entry -> {
                    Product product = productsById.getOrDefault(entry.getKey(), allOrders.stream()
                            .map(CustomerOrder::getProduct)
                            .filter(candidate -> candidate.getId().equals(entry.getKey()))
                            .findFirst()
                            .orElseThrow());
                    ProductTotals productTotals = entry.getValue();
                    return new ProductPerformance(
                            product.getId(),
                            product.getSku(),
                            product.getName(),
                            productTotals.units(),
                            productTotals.revenue(),
                            product.getStockQuantity());
                })
                .toList();
    }

    private List<CategoryPerformance> categoryPerformance(List<CustomerOrder> allOrders, List<Product> activeProducts) {
        Map<String, CategoryTotals> totals = new HashMap<>();
        for (Product product : activeProducts) {
            String category = categoryName(product);
            totals.computeIfAbsent(category, value -> new CategoryTotals())
                    .addProduct(product.getUnitPrice().multiply(BigDecimal.valueOf(product.getStockQuantity())));
        }
        for (CustomerOrder order : allOrders) {
            if (order.getStatus() == OrderStatus.CANCELLED) {
                continue;
            }
            totals.computeIfAbsent(categoryName(order.getProduct()), value -> new CategoryTotals()).add(order);
        }
        return totals.entrySet().stream()
                .sorted(Comparator.comparing((Map.Entry<String, CategoryTotals> entry) -> entry.getValue().revenue()).reversed())
                .map(entry -> new CategoryPerformance(
                        entry.getKey(),
                        entry.getValue().productCount(),
                        entry.getValue().units(),
                        entry.getValue().revenue(),
                        entry.getValue().stockValue()))
                .toList();
    }

    private List<SupplierPerformance> supplierPerformance(List<Product> activeProducts) {
        Map<String, SupplierTotals> totals = new HashMap<>();
        for (Product product : activeProducts) {
            String supplier = product.getSupplier() == null ? "Unassigned" : product.getSupplier().getName();
            totals.computeIfAbsent(supplier, value -> new SupplierTotals())
                    .add(product.getUnitPrice().multiply(BigDecimal.valueOf(product.getStockQuantity())));
        }
        return totals.entrySet().stream()
                .sorted(Comparator.comparing((Map.Entry<String, SupplierTotals> entry) -> entry.getValue().stockValue()).reversed())
                .map(entry -> new SupplierPerformance(entry.getKey(), entry.getValue().productCount(), entry.getValue().stockValue()))
                .toList();
    }

    private List<MonthlyRevenue> monthlyRevenue(List<CustomerOrder> allOrders) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, MonthTotals> totals = new HashMap<>();
        for (CustomerOrder order : allOrders) {
            if (order.getStatus() == OrderStatus.CANCELLED) {
                continue;
            }
            String month = order.getOrderDate().format(formatter);
            totals.computeIfAbsent(month, value -> new MonthTotals()).add(order);
        }
        return totals.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new MonthlyRevenue(entry.getKey(), entry.getValue().orderCount(), entry.getValue().revenue()))
                .toList();
    }

    private List<String> businessInsights(
            List<Product> activeProducts,
            List<CustomerOrder> allOrders,
            BigDecimal inventoryValue,
            double stockTurnoverRatio,
            long unsuppliedProducts) {
        List<String> insights = new ArrayList<>();
        long lowStockCount = activeProducts.stream()
                .filter(product -> product.getStockQuantity() <= product.getLowStockThreshold())
                .count();
        long cancelledOrders = allOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.CANCELLED)
                .count();
        if (lowStockCount > 0) {
            insights.add(lowStockCount + " product(s) are at or below their reorder threshold.");
        }
        if (unsuppliedProducts > 0) {
            insights.add(unsuppliedProducts + " active product(s) are not linked to a supplier.");
        }
        if (stockTurnoverRatio > 0 && stockTurnoverRatio < 0.5) {
            insights.add("Stock turnover is low, so inventory may be moving slowly.");
        }
        if (cancelledOrders > 0) {
            insights.add(cancelledOrders + " order(s) were cancelled; review stock availability and customer follow-up.");
        }
        if (inventoryValue.compareTo(BigDecimal.ZERO) > 0) {
            insights.add("Current inventory value is " + inventoryValue.setScale(2, RoundingMode.HALF_UP) + ".");
        }
        if (insights.isEmpty()) {
            insights.add("No major operational risks detected from the current data.");
        }
        return insights;
    }

    private String categoryName(Product product) {
        return product.getCategory() == null || product.getCategory().isBlank() ? "Uncategorized" : product.getCategory();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record DeliveryCounts(long onTime, long delayed) {}

    private static class ProductTotals {
        private long units;
        private BigDecimal revenue = BigDecimal.ZERO;

        void add(CustomerOrder order) {
            units += order.getQuantity();
            revenue = revenue.add(order.getTotalAmount());
        }

        long units() {
            return units;
        }

        BigDecimal revenue() {
            return revenue;
        }
    }

    private static final class CategoryTotals extends ProductTotals {
        private long productCount;
        private BigDecimal stockValue = BigDecimal.ZERO;

        void addProduct(BigDecimal value) {
            productCount++;
            stockValue = stockValue.add(value);
        }

        long productCount() {
            return productCount;
        }

        BigDecimal stockValue() {
            return stockValue;
        }
    }

    private static final class SupplierTotals {
        private long productCount;
        private BigDecimal stockValue = BigDecimal.ZERO;

        void add(BigDecimal value) {
            productCount++;
            stockValue = stockValue.add(value);
        }

        long productCount() {
            return productCount;
        }

        BigDecimal stockValue() {
            return stockValue;
        }
    }

    private static final class MonthTotals {
        private long orderCount;
        private BigDecimal revenue = BigDecimal.ZERO;

        void add(CustomerOrder order) {
            orderCount++;
            revenue = revenue.add(order.getTotalAmount());
        }

        long orderCount() {
            return orderCount;
        }

        BigDecimal revenue() {
            return revenue;
        }
    }
}

