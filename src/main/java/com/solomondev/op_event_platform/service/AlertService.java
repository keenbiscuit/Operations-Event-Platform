package com.solomondev.op_event_platform.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solomondev.op_event_platform.entity.Alert;
import com.solomondev.op_event_platform.entity.AlertEvent;
import com.solomondev.op_event_platform.model.dto.AlertEventSummaryDto;
import com.solomondev.op_event_platform.model.dto.AlertResponseDto;
import com.solomondev.op_event_platform.model.dto.PageResponse;
import com.solomondev.op_event_platform.model.exception.InvalidAlertStateException;
import com.solomondev.op_event_platform.model.exception.ResourceNotFoundException;
import com.solomondev.op_event_platform.repository.AlertEventRepository;
import com.solomondev.op_event_platform.repository.AlertRepository;

@Service
public class AlertService {
    private final AlertRepository alertRepository;
    private final AlertEventRepository alertEventRepository;
    private final CurrentUserService currentUserService;

    public AlertService(
            AlertRepository alertRepository,
            AlertEventRepository alertEventRepository,
            CurrentUserService currentUserService) {
        this.alertRepository = alertRepository;
        this.alertEventRepository = alertEventRepository;
        this.currentUserService = currentUserService;
    }

    public PageResponse<AlertResponseDto> searchAlerts(
            String status,
            String severity,
            Long assetId,
            String query,
            LocalDateTime fromTime,
            LocalDateTime toTime,
            int page,
            int size,
            Sort.Direction direction) {
        String subject = currentUserService.requireSubject();
        int safeSize = Math.min(size, 100);
        Page<Alert> alerts = alertRepository.searchAccessible(
                subject,
                blankToNull(status),
                blankToNull(severity),
                assetId,
                blankToEmpty(query),
                fromBound(fromTime),
                toBound(toTime),
                PageRequest.of(page, safeSize, Sort.by(direction, "createdAt").and(Sort.by(direction, "id"))));

        Map<Long, List<AlertEventSummaryDto>> eventsByAlert = loadEventSummaries(alerts.getContent());
        return PageResponse.from(alerts.map(alert -> mapToResponseDto(alert,
                eventsByAlert.getOrDefault(alert.getId(), List.of()))));
    }

    @Transactional(readOnly = true)
    public AlertResponseDto getAlertById(Long id) {
        Alert alert = findAccessibleAlert(id);
        return mapToResponseDto(alert, loadEventSummaries(List.of(alert))
                .getOrDefault(alert.getId(), List.of()));
    }

    @Transactional
    public AlertResponseDto acknowledgeAlert(Long id) {
        Alert alert = findAccessibleAlert(id);

        if ("RESOLVED".equals(alert.getStatus())) {
            throw new InvalidAlertStateException("Resolved alerts cannot be acknowledged");
        }

        alert.setStatus("ACKNOWLEDGED");
        alert.setAcknowledgedAt(LocalDateTime.now());
        Alert saved = alertRepository.save(alert);
        return mapToResponseDto(saved, loadEventSummaries(List.of(saved))
                .getOrDefault(saved.getId(), List.of()));
    }

    @Transactional
    public AlertResponseDto resolveAlert(Long id) {
        Alert alert = findAccessibleAlert(id);

        if ("RESOLVED".equals(alert.getStatus())) {
            throw new InvalidAlertStateException("Resolved alerts cannot be resolved again");
        }

        alert.setStatus("RESOLVED");
        alert.setResolvedAt(LocalDateTime.now());
        Alert saved = alertRepository.save(alert);
        return mapToResponseDto(saved, loadEventSummaries(List.of(saved))
                .getOrDefault(saved.getId(), List.of()));
    }

    private Alert findAccessibleAlert(Long id) {
        return alertRepository.findAccessibleById(id, currentUserService.requireSubject())
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));
    }

    private Map<Long, List<AlertEventSummaryDto>> loadEventSummaries(List<Alert> alerts) {
        if (alerts.isEmpty()) {
            return Map.of();
        }

        List<Long> alertIds = alerts.stream().map(Alert::getId).toList();
        return alertEventRepository.findAllByAlert_IdIn(alertIds).stream()
                .collect(Collectors.groupingBy(
                        link -> link.getAlert().getId(),
                        Collectors.mapping(this::mapEventSummary, Collectors.toList())));
    }

    private AlertEventSummaryDto mapEventSummary(AlertEvent link) {
        return new AlertEventSummaryDto(
                link.getEvent().getId(),
                link.getEvent().getAsset().getId(),
                link.getEvent().getAsset().getName(),
                link.getEvent().getEventType(),
                link.getEvent().getValue(),
                link.getEvent().getOccurredAt(),
                link.getLinkedAt());
    }

    private AlertResponseDto mapToResponseDto(Alert alert, List<AlertEventSummaryDto> events) {
        return new AlertResponseDto(
                alert.getId(),
                alert.getRuleAssignment().getId(),
                alert.getRuleAssignment().getAsset().getId(),
                alert.getRuleAssignment().getAsset().getName(),
                alert.getStatus(),
                alert.getSeverity(),
                alert.getNotificationCount(),
                alert.getCreatedAt(),
                alert.getAcknowledgedAt(),
                alert.getResolvedAt(),
                alert.getLastNotifiedAt(),
                events);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private LocalDateTime fromBound(LocalDateTime value) {
        return value == null ? LocalDateTime.of(1, 1, 1, 0, 0) : value;
    }

    private LocalDateTime toBound(LocalDateTime value) {
        return value == null ? LocalDateTime.of(9999, 12, 31, 23, 59, 59, 999_999_999) : value;
    }

    private String blankToEmpty(String value) {
        return value == null || value.isBlank() ? "" : value.trim();
    }
}

