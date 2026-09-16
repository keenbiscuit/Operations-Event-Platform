package com.solomondev.op_event_platform.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.solomondev.op_event_platform.entity.Alert;
import com.solomondev.op_event_platform.model.dto.AlertResponseDto;
import com.solomondev.op_event_platform.model.exception.InvalidAlertStateException;
import com.solomondev.op_event_platform.model.exception.ResourceNotFoundException;
import com.solomondev.op_event_platform.repository.AlertRepository;

@Service
public class AlertService {
    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    public List<AlertResponseDto> getAllAlerts() {

        return alertRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .toList();

    }

    public List<AlertResponseDto> getAlertsByStatus(String status) {

        return alertRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    public AlertResponseDto getAlertById(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alert not found with id: " + id));

        return mapToResponseDto(alert);
    }

    public List<AlertResponseDto> getAlertByRuleAssignmentAssetId(Long id) {

        return alertRepository.findByRuleAssignment_Asset_Id(id)
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    public List<AlertResponseDto> getAlertsByAssetIdAndStatus(Long assetId, String status) {

        return alertRepository.findByRuleAssignment_Asset_IdAndStatus(assetId, status)
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    public AlertResponseDto acknowledgeAlert(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alert not found with id: " + id));

        if ("RESOLVED".equals(alert.getStatus())) {
            throw new InvalidAlertStateException("Resolved alerts cannot be acknowledged");
        }

        alert.setStatus("ACKNOWLEDGED");
        alert.setAcknowledgedAt(LocalDateTime.now());

        alertRepository.save(alert);

        return mapToResponseDto(alert);

    }

    public AlertResponseDto resolveAlert(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alert not found with id: " + id));

        if ("RESOLVED".equals(alert.getStatus())) {
            throw new InvalidAlertStateException("Resolved alerts cannot be resolved again");
        }

        alert.setStatus("RESOLVED");
        alert.setResolvedAt(LocalDateTime.now());

        alertRepository.save(alert);

        return mapToResponseDto(alert);
    }

    private AlertResponseDto mapToResponseDto(Alert alert) {

        AlertResponseDto dto = new AlertResponseDto();
        dto.setId(alert.getId());
        dto.setRuleAssignmentId(alert.getRuleAssignment().getId());
        dto.setStatus(alert.getStatus());
        dto.setSeverity(alert.getSeverity());
        dto.setNotificationCount(alert.getNotificationCount());
        dto.setCreatedAt(alert.getCreatedAt());
        dto.setAcknowledgedAt(alert.getAcknowledgedAt());
        dto.setResolvedAt(alert.getResolvedAt());
        dto.setLastNotifiedAt(alert.getLastNotifiedAt());

        return dto;
    }
}
