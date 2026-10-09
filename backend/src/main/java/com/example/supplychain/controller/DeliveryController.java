package com.example.supplychain.controller;

import com.example.supplychain.domain.OrderStatus;
import com.example.supplychain.dto.OrderResponse;
import com.example.supplychain.service.OrderService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {
    private final OrderService orders;

    public DeliveryController(OrderService orders) {
        this.orders = orders;
    }

    @GetMapping
    List<OrderResponse> list() {
        return orders.list(null).stream()
                .filter(order -> order.status() == OrderStatus.SHIPPED || order.status() == OrderStatus.DELIVERED)
                .toList();
    }
}

