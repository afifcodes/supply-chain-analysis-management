package com.example.supplychain.dto;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
        long totalProducts,
        long totalSuppliers,
        long totalOrders,
        long pendingOrders,
        long lowStockProducts,
        long deliveredOnTime,
        long delayedDeliveries,
        BigDecimal totalOrderValue
) {}

