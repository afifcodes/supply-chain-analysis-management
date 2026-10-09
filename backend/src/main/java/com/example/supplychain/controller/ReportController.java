package com.example.supplychain.controller;

import com.example.supplychain.dto.ReportsSummaryResponse;
import com.example.supplychain.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final DashboardService dashboard;

    public ReportController(DashboardService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/summary")
    ReportsSummaryResponse summary() {
        return dashboard.reports();
    }
}
