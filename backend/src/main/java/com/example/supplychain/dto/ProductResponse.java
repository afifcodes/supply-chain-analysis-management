package com.example.supplychain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        String category,
        BigDecimal unitPrice,
        Integer stockQuantity,
        Integer lowStockThreshold,
        boolean lowStock,
        Long supplierId,
        String supplierName,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

