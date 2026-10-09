package com.example.supplychain.service;

import com.example.supplychain.domain.CustomerOrder;
import com.example.supplychain.domain.OrderStatus;
import com.example.supplychain.domain.Product;
import com.example.supplychain.dto.OrderRequest;
import com.example.supplychain.dto.OrderResponse;
import com.example.supplychain.dto.OrderStatusRequest;
import com.example.supplychain.exception.BusinessRuleException;
import com.example.supplychain.exception.ResourceNotFoundException;
import com.example.supplychain.repository.CustomerOrderRepository;
import com.example.supplychain.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private final CustomerOrderRepository orders;
    private final ProductRepository products;
    private final DeliveryCalculator deliveryCalculator;
    private final Clock clock;

    public OrderService(CustomerOrderRepository orders, ProductRepository products, DeliveryCalculator deliveryCalculator, Clock clock) {
        this.orders = orders;
        this.products = products;
        this.deliveryCalculator = deliveryCalculator;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> list(OrderStatus status) {
        List<CustomerOrder> result = status == null
                ? orders.findAllByOrderByOrderDateDesc()
                : orders.findByStatusOrderByOrderDateDesc(status);
        return result.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> recent(int limit) {
        return orders.findAllByOrderByOrderDateDesc(PageRequest.of(0, Math.max(1, limit)))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public OrderResponse create(OrderRequest request) {
        Product product = products.findActiveByIdForUpdate(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));
        if (product.getStockQuantity() < request.quantity()) {
            throw new BusinessRuleException("Insufficient stock for this order.");
        }

        product.setStockQuantity(product.getStockQuantity() - request.quantity());

        CustomerOrder order = new CustomerOrder();
        order.setOrderNumber(generateOrderNumber());
        order.setProduct(product);
        order.setQuantity(request.quantity());
        order.setUnitPrice(product.getUnitPrice());
        order.setTotalAmount(product.getUnitPrice().multiply(BigDecimal.valueOf(request.quantity())));
        order.setCustomerName(blankToNull(request.customerName()));
        order.setCustomerContact(blankToNull(request.customerContact()));
        order.setExpectedDeliveryDate(request.expectedDeliveryDate());
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now(clock));
        return toResponse(orders.save(order));
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatusRequest request) {
        CustomerOrder order = find(id);
        OrderStatus current = order.getStatus();
        OrderStatus next = request.status();
        if (current == next) {
            return toResponse(order);
        }

        if (current == OrderStatus.PENDING && next == OrderStatus.CANCELLED) {
            restorePendingOrderStock(order);
            order.setStatus(OrderStatus.CANCELLED);
            return toResponse(order);
        }
        if (current == OrderStatus.PENDING && next == OrderStatus.SHIPPED) {
            order.setStatus(OrderStatus.SHIPPED);
            return toResponse(order);
        }
        if (current == OrderStatus.SHIPPED && next == OrderStatus.DELIVERED) {
            order.setStatus(OrderStatus.DELIVERED);
            order.setActualDeliveryDate(request.actualDeliveryDate() == null ? LocalDate.now(clock) : request.actualDeliveryDate());
            return toResponse(order);
        }

        throw new BusinessRuleException("Invalid order status transition from " + current + " to " + next + ".");
    }

    CustomerOrder find(Long id) {
        return orders.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found."));
    }

    OrderResponse toResponse(CustomerOrder order) {
        Product product = order.getProduct();
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                product.getId(),
                product.getName(),
                product.getSku(),
                order.getQuantity(),
                order.getUnitPrice(),
                order.getTotalAmount(),
                order.getCustomerName(),
                order.getCustomerContact(),
                order.getStatus(),
                order.getOrderDate(),
                order.getExpectedDeliveryDate(),
                order.getActualDeliveryDate(),
                deliveryCalculator.isDelayed(order),
                deliveryCalculator.delayDays(order),
                order.getCreatedAt(),
                order.getUpdatedAt());
    }

    private void restorePendingOrderStock(CustomerOrder order) {
        if (order.isStockRestored()) {
            return;
        }
        Product product = products.findActiveByIdForUpdate(order.getProduct().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));
        product.setStockQuantity(product.getStockQuantity() + order.getQuantity());
        order.setStockRestored(true);
    }

    private String generateOrderNumber() {
        String prefix = "ORD-" + LocalDate.now(clock).format(DateTimeFormatter.BASIC_ISO_DATE) + "-";
        return prefix + System.nanoTime();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
