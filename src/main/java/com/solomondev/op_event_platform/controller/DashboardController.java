package com.solomondev.op_event_platform.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.solomondev.op_event_platform.model.dto.DashboardSummaryDto;
import com.solomondev.op_event_platform.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }
}
