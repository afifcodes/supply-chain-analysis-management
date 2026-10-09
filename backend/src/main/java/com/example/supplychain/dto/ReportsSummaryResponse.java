package com.example.supplychain.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ReportsSummaryResponse(
        Map<String, Long> orderCountByStatus,
        List<ProductResponse> lowStockProducts,
        long deliveredOnTime,
        long delayedDeliveries,
        BigDecimal totalOrderValue
) {}

