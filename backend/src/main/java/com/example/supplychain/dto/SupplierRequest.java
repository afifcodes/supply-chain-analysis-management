package com.example.supplychain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupplierRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 120) String contactPerson,
        @Email @Size(max = 190) String email,
        @Size(max = 40) String phone,
        String address,
        String notes
) {}

