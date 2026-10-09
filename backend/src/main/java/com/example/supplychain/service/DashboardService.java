package com.example.supplychain.service;

import com.example.supplychain.domain.CustomerOrder;
import com.example.supplychain.domain.OrderStatus;
import com.example.supplychain.dto.DashboardSummaryResponse;
import com.example.supplychain.dto.ReportsSummaryResponse;
import com.example.supplychain.repository.CustomerOrderRepository;
import com.example.supplychain.repository.ProductRepository;
import com.example.supplychain.repository.SupplierRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private final ProductRepository products;
    private final SupplierRepository suppliers;
    private final CustomerOrderRepository orders;
    private final ProductService productService;
    private final DeliveryCalculator deliveryCalculator;

    public DashboardService(
            ProductRepository products,
            SupplierRepository suppliers,
            CustomerOrderRepository orders,
            ProductService productService,
            DeliveryCalculator deliveryCalculator) {
        this.products = products;
        this.suppliers = suppliers;
        this.orders = orders;
        this.productService = productService;
        this.deliveryCalculator = deliveryCalculator;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary() {
        DeliveryCounts deliveryCounts = deliveryCounts();
        return new DashboardSummaryResponse(
                products.countByActiveTrue(),
                suppliers.count(),
                orders.count(),
                orders.countByStatus(OrderStatus.PENDING),
                products.countLowStockProducts(),
                deliveryCounts.onTime(),
                deliveryCounts.delayed(),
                orders.sumTotalAmount());
    }

    @Transactional(readOnly = true)
    public ReportsSummaryResponse reports() {
        DeliveryCounts deliveryCounts = deliveryCounts();
        Map<String, Long> counts = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            counts.put(status.name(), orders.countByStatus(status));
        }
        return new ReportsSummaryResponse(
                counts,
                productService.list(null, true),
                deliveryCounts.onTime(),
                deliveryCounts.delayed(),
                orders.sumTotalAmount());
    }

    private DeliveryCounts deliveryCounts() {
        long delayed = 0;
        long onTime = 0;
        for (CustomerOrder order : orders.findAll()) {
            if (order.getStatus() == OrderStatus.CANCELLED || order.getExpectedDeliveryDate() == null) {
                continue;
            }
            if (deliveryCalculator.isDelayed(order)) {
                delayed++;
            } else if (order.getStatus() == OrderStatus.DELIVERED) {
                onTime++;
            }
        }
        return new DeliveryCounts(onTime, delayed);
    }

    private record DeliveryCounts(long onTime, long delayed) {}
}

