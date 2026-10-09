package com.example.supplychain.dto;

import com.example.supplychain.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        String orderNumber,
        Long productId,
        String productName,
        String productSku,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        String customerName,
        String customerContact,
        OrderStatus status,
        LocalDateTime orderDate,
        LocalDate expectedDeliveryDate,
        LocalDate actualDeliveryDate,
        boolean delayed,
        Long delayDays,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

