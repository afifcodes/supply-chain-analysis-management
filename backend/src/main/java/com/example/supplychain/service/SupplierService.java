package com.example.supplychain.service;

import com.example.supplychain.domain.Supplier;
import com.example.supplychain.dto.SupplierRequest;
import com.example.supplychain.dto.SupplierResponse;
import com.example.supplychain.exception.BusinessRuleException;
import com.example.supplychain.exception.ResourceNotFoundException;
import com.example.supplychain.repository.ProductRepository;
import com.example.supplychain.repository.SupplierRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupplierService {
    private final SupplierRepository suppliers;
    private final ProductRepository products;

    public SupplierService(SupplierRepository suppliers, ProductRepository products) {
        this.suppliers = suppliers;
        this.products = products;
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> list(String search) {
        List<Supplier> result = search == null || search.isBlank()
                ? suppliers.findAll()
                : suppliers.findByNameContainingIgnoreCaseOrderByNameAsc(search.trim());
        return result.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SupplierResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        Supplier supplier = new Supplier();
        apply(supplier, request);
        return toResponse(suppliers.save(supplier));
    }

    @Transactional
    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = find(id);
        apply(supplier, request);
        return toResponse(supplier);
    }

    @Transactional
    public void delete(Long id) {
        Supplier supplier = find(id);
        boolean used = products.findAll().stream()
                .anyMatch(product -> product.getSupplier() != null && product.getSupplier().getId().equals(id));
        if (used) {
            throw new BusinessRuleException("Supplier cannot be deleted while products reference it.");
        }
        suppliers.delete(supplier);
    }

    Supplier find(Long id) {
        return suppliers.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found."));
    }

    private void apply(Supplier supplier, SupplierRequest request) {
        supplier.setName(request.name().trim());
        supplier.setContactPerson(blankToNull(request.contactPerson()));
        supplier.setEmail(blankToNull(request.email()));
        supplier.setPhone(blankToNull(request.phone()));
        supplier.setAddress(blankToNull(request.address()));
        supplier.setNotes(blankToNull(request.notes()));
    }

    private SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getContactPerson(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.getAddress(),
                supplier.getNotes(),
                supplier.getCreatedAt(),
                supplier.getUpdatedAt());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

