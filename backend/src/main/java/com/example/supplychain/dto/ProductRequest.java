package com.example.supplychain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank @Size(max = 60) String sku,
        @NotBlank @Size(max = 160) String name,
        String description,
        @Size(max = 100) String category,
        @NotNull @DecimalMin(value = "0.00") BigDecimal unitPrice,
        @NotNull @Min(0) Integer stockQuantity,
        @NotNull @Min(0) Integer lowStockThreshold,
        Long supplierId
) {}

