package com.example.supplychain.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ReportsSummaryResponse(
        Map<String, Long> orderCountByStatus,
        List<ProductResponse> lowStockProducts,
        long deliveredOnTime,
        long delayedDeliveries,
        BigDecimal totalOrderValue,
        AnalyticsSummary analytics
) {
    public record AnalyticsSummary(
            BigDecimal inventoryValue,
            BigDecimal averageOrderValue,
            double averageOrderQuantity,
            double stockTurnoverRatio,
            long totalUnitsSold,
            long unsuppliedProducts,
            List<ProductPerformance> topProducts,
            List<CategoryPerformance> categoryPerformance,
            List<SupplierPerformance> supplierPerformance,
            List<MonthlyRevenue> monthlyRevenue,
            List<String> businessInsights
    ) {}

    public record ProductPerformance(
            Long productId,
            String sku,
            String name,
            long unitsSold,
            BigDecimal revenue,
            int currentStock
    ) {}

    public record CategoryPerformance(
            String category,
            long productCount,
            long unitsSold,
            BigDecimal revenue,
            BigDecimal stockValue
    ) {}

    public record SupplierPerformance(
            String supplier,
            long productCount,
            BigDecimal stockValue
    ) {}

    public record MonthlyRevenue(
            String month,
            long orderCount,
            BigDecimal revenue
    ) {}
}

