package com.solomondev.op_event_platform.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AlertEventSummaryDto(
        Long id,
        Long assetId,
        String assetName,
        String eventType,
        BigDecimal value,
        LocalDateTime occurredAt,
        LocalDateTime linkedAt) {
}
