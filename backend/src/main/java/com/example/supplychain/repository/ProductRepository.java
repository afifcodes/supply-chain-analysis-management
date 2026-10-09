package com.example.supplychain.repository;

import com.example.supplychain.domain.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsBySkuIgnoreCase(String sku);
    boolean existsBySkuIgnoreCaseAndIdNot(String sku, Long id);
    long countByActiveTrue();
    long countByActiveTrueAndStockQuantityLessThanEqual(Integer threshold);

    @Query("select count(p) from Product p where p.active = true and p.stockQuantity <= p.lowStockThreshold")
    long countLowStockProducts();

    List<Product> findByActiveTrueAndNameContainingIgnoreCaseOrActiveTrueAndSkuContainingIgnoreCaseOrderByNameAsc(String name, String sku);
    List<Product> findByActiveTrueOrderByNameAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id and p.active = true")
    Optional<Product> findActiveByIdForUpdate(Long id);
}

