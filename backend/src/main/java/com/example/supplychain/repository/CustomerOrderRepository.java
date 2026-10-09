package com.example.supplychain.repository;

import com.example.supplychain.domain.CustomerOrder;
import com.example.supplychain.domain.OrderStatus;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    boolean existsByProductId(Long productId);
    long countByStatus(OrderStatus status);
    List<CustomerOrder> findAllByOrderByOrderDateDesc();
    List<CustomerOrder> findAllByOrderByOrderDateDesc(Pageable pageable);
    List<CustomerOrder> findByStatusOrderByOrderDateDesc(OrderStatus status);

    @Query("select coalesce(sum(o.totalAmount), 0) from CustomerOrder o")
    BigDecimal sumTotalAmount();
}

