package com.solomondev.op_event_platform.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record EventResponseDto(
        Long id,
        Long assetId,
        String assetName,
        String eventType,
        BigDecimal value,
        LocalDateTime occurredAt,
        List<EventAlertSummaryDto> alerts) {
}
