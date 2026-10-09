package com.example.supplychain.controller;

import com.example.supplychain.dto.SupplierRequest;
import com.example.supplychain.dto.SupplierResponse;
import com.example.supplychain.service.SupplierService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {
    private final SupplierService suppliers;

    public SupplierController(SupplierService suppliers) {
        this.suppliers = suppliers;
    }

    @GetMapping
    List<SupplierResponse> list(@RequestParam(required = false) String search) {
        return suppliers.list(search);
    }

    @GetMapping("/{id}")
    SupplierResponse get(@PathVariable Long id) {
        return suppliers.get(id);
    }

    @PostMapping
    ResponseEntity<SupplierResponse> create(@Valid @RequestBody SupplierRequest request) {
        SupplierResponse response = suppliers.create(request);
        return ResponseEntity.created(URI.create("/api/suppliers/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    SupplierResponse update(@PathVariable Long id, @Valid @RequestBody SupplierRequest request) {
        return suppliers.update(id, request);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable Long id) {
        suppliers.delete(id);
        return ResponseEntity.noContent().build();
    }
}

