package com.example.supplychain.service;

import com.example.supplychain.domain.Product;
import com.example.supplychain.domain.Supplier;
import com.example.supplychain.dto.ProductRequest;
import com.example.supplychain.dto.ProductResponse;
import com.example.supplychain.exception.BusinessRuleException;
import com.example.supplychain.exception.ResourceNotFoundException;
import com.example.supplychain.repository.CustomerOrderRepository;
import com.example.supplychain.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private final ProductRepository products;
    private final CustomerOrderRepository orders;
    private final SupplierService supplierService;

    public ProductService(ProductRepository products, CustomerOrderRepository orders, SupplierService supplierService) {
        this.products = products;
        this.orders = orders;
        this.supplierService = supplierService;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list(String search, boolean lowStockOnly) {
        List<Product> result = search == null || search.isBlank()
                ? products.findByActiveTrueOrderByNameAsc()
                : products.findByActiveTrueAndNameContainingIgnoreCaseOrActiveTrueAndSkuContainingIgnoreCaseOrderByNameAsc(search.trim(), search.trim());
        return result.stream()
                .filter(product -> !lowStockOnly || isLowStock(product))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return toResponse(findActive(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (products.existsBySkuIgnoreCase(request.sku().trim())) {
            throw new BusinessRuleException("SKU already exists.");
        }
        Product product = new Product();
        apply(product, request);
        return toResponse(products.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findActive(id);
        if (products.existsBySkuIgnoreCaseAndIdNot(request.sku().trim(), id)) {
            throw new BusinessRuleException("SKU already exists.");
        }
        apply(product, request);
        return toResponse(product);
    }

    @Transactional
    public void delete(Long id) {
        Product product = findActive(id);
        if (orders.existsByProductId(id)) {
            product.setActive(false);
            return;
        }
        products.delete(product);
    }

    Product findActive(Long id) {
        return products.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));
    }

    ProductResponse toResponse(Product product) {
        Supplier supplier = product.getSupplier();
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getUnitPrice(),
                product.getStockQuantity(),
                product.getLowStockThreshold(),
                isLowStock(product),
                supplier == null ? null : supplier.getId(),
                supplier == null ? null : supplier.getName(),
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }

    private void apply(Product product, ProductRequest request) {
        product.setSku(request.sku().trim());
        product.setName(request.name().trim());
        product.setDescription(blankToNull(request.description()));
        product.setCategory(blankToNull(request.category()));
        product.setUnitPrice(request.unitPrice());
        product.setStockQuantity(request.stockQuantity());
        product.setLowStockThreshold(request.lowStockThreshold());
        product.setSupplier(request.supplierId() == null ? null : supplierService.find(request.supplierId()));
    }

    private boolean isLowStock(Product product) {
        return product.getStockQuantity() <= product.getLowStockThreshold();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

