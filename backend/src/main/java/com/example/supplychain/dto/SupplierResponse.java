package com.example.supplychain.dto;

import java.time.LocalDateTime;

public record SupplierResponse(
        Long id,
        String name,
        String contactPerson,
        String email,
        String phone,
        String address,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

