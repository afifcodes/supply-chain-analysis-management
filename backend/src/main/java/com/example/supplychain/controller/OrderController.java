package com.example.supplychain.controller;

import com.example.supplychain.domain.OrderStatus;
import com.example.supplychain.dto.OrderRequest;
import com.example.supplychain.dto.OrderResponse;
import com.example.supplychain.dto.OrderStatusRequest;
import com.example.supplychain.service.OrderService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @GetMapping
    List<OrderResponse> list(@RequestParam(required = false) OrderStatus status) {
        return orders.list(status);
    }

    @GetMapping("/{id}")
    OrderResponse get(@PathVariable Long id) {
        return orders.get(id);
    }

    @PostMapping
    ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        OrderResponse response = orders.create(request);
        return ResponseEntity.created(URI.create("/api/orders/" + response.id())).body(response);
    }

    @PatchMapping("/{id}/status")
    OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusRequest request) {
        return orders.updateStatus(id, request);
    }
}

