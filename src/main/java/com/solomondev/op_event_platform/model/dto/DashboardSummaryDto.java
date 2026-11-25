package com.solomondev.op_event_platform.model.dto;

public record DashboardSummaryDto(
        long totalEvents,
        long activeAlerts,
        long acknowledgedAlerts,
        long resolvedAlerts) {
}
