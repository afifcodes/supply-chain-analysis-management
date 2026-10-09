package com.example.supplychain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.supplychain.domain.OrderStatus;
import com.example.supplychain.dto.OrderRequest;
import com.example.supplychain.dto.OrderResponse;
import com.example.supplychain.dto.OrderStatusRequest;
import com.example.supplychain.dto.ProductRequest;
import com.example.supplychain.exception.BusinessRuleException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OrderServiceTest {
    @Autowired
    ProductService productService;

    @Autowired
    OrderService orderService;

    Long productId;

    @BeforeEach
    void setUp() {
        ProductRequest request = new ProductRequest(
                "SKU-" + System.nanoTime(),
                "Test Product",
                null,
                "Testing",
                new BigDecimal("25.50"),
                10,
                3,
                null);
        productId = productService.create(request).id();
    }

    @Test
    void creatingOrderDecreasesStockAndStoresPriceSnapshot() {
        OrderResponse order = orderService.create(new OrderRequest(productId, 4, "Student", null, LocalDate.now().plusDays(3)));

        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.unitPrice()).isEqualByComparingTo("25.50");
        assertThat(order.totalAmount()).isEqualByComparingTo("102.00");
        assertThat(productService.get(productId).stockQuantity()).isEqualTo(6);
    }

    @Test
    void insufficientStockIsRejectedWithoutChangingStock() {
        assertThatThrownBy(() -> orderService.create(new OrderRequest(productId, 99, "Student", null, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Insufficient stock");

        assertThat(productService.get(productId).stockQuantity()).isEqualTo(10);
    }

    @Test
    void pendingCancellationRestoresStockExactlyOnce() {
        OrderResponse order = orderService.create(new OrderRequest(productId, 2, "Student", null, null));
        assertThat(productService.get(productId).stockQuantity()).isEqualTo(8);

        orderService.updateStatus(order.id(), new OrderStatusRequest(OrderStatus.CANCELLED, null));
        orderService.updateStatus(order.id(), new OrderStatusRequest(OrderStatus.CANCELLED, null));

        assertThat(productService.get(productId).stockQuantity()).isEqualTo(10);
    }

    @Test
    void invalidStatusTransitionsAreRejected() {
        OrderResponse order = orderService.create(new OrderRequest(productId, 1, "Student", null, null));

        assertThatThrownBy(() -> orderService.updateStatus(order.id(), new OrderStatusRequest(OrderStatus.DELIVERED, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Invalid order status transition");
    }

    @Test
    void shippedOrderCanBeDeliveredWithActualDate() {
        OrderResponse order = orderService.create(new OrderRequest(productId, 1, "Student", null, LocalDate.now().plusDays(1)));
        orderService.updateStatus(order.id(), new OrderStatusRequest(OrderStatus.SHIPPED, null));

        OrderResponse delivered = orderService.updateStatus(order.id(), new OrderStatusRequest(OrderStatus.DELIVERED, LocalDate.now().plusDays(1)));

        assertThat(delivered.status()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(delivered.actualDeliveryDate()).isEqualTo(LocalDate.now().plusDays(1));
        assertThat(delivered.delayed()).isFalse();
    }
}

