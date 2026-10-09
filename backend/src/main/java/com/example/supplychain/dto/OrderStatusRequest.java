package com.example.supplychain.dto;

import com.example.supplychain.domain.OrderStatus;
import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;

public record OrderStatusRequest(
        @NotNull OrderStatus status,
        LocalDate actualDeliveryDate
) {}

