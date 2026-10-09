package com.example.supplychain.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record OrderRequest(
        @NotNull Long productId,
        @NotNull @Min(1) Integer quantity,
        @Size(max = 150) String customerName,
        @Size(max = 100) String customerContact,
        @FutureOrPresent LocalDate expectedDeliveryDate
) {}

