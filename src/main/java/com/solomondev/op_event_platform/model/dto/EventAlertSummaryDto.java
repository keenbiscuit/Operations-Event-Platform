package com.solomondev.op_event_platform.model.dto;

import java.time.LocalDateTime;

public record EventAlertSummaryDto(
        Long id,
        String status,
        String severity,
        LocalDateTime createdAt) {
}
