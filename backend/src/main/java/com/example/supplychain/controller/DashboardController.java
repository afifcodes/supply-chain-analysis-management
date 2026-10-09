package com.example.supplychain.controller;

import com.example.supplychain.dto.DashboardSummaryResponse;
import com.example.supplychain.dto.OrderResponse;
import com.example.supplychain.service.DashboardService;
import com.example.supplychain.service.OrderService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboard;
    private final OrderService orders;

    public DashboardController(DashboardService dashboard, OrderService orders) {
        this.dashboard = dashboard;
        this.orders = orders;
    }

    @GetMapping("/summary")
    DashboardSummaryResponse summary() {
        return dashboard.summary();
    }

    @GetMapping("/recent-orders")
    List<OrderResponse> recentOrders(@RequestParam(defaultValue = "5") int limit) {
        return orders.recent(limit);
    }
}

