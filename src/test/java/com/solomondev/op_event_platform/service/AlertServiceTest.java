package com.solomondev.op_event_platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import com.solomondev.op_event_platform.repository.AlertRepository;
import com.solomondev.op_event_platform.entity.*;
import com.solomondev.op_event_platform.model.dto.AlertResponseDto;
import com.solomondev.op_event_platform.model.exception.InvalidAlertStateException;
import com.solomondev.op_event_platform.model.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class AlertServiceTest {
    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private AlertService alertService;

    private Alert alert1;
    private Alert alert2;
    private Alert alert3;

    private RuleAssignment instance1;
    private RuleAssignment instance2;
    private RuleAssignment instance3;

    private Asset asset1;
    private Asset asset2;

    @BeforeEach
    void setUp() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 9, 7, 14, 0);

        asset1 = new Asset();
        asset1.setId(1L);
        asset1.setName("Test Asset");
        asset1.setType("API");

        asset2 = new Asset();
        asset2.setId(2L);
        asset2.setName("Order Database");
        asset2.setType("DATABASE");

        instance1 = new RuleAssignment();
        instance1.setId(10L);
        instance1.setAsset(asset1);

        instance2 = new RuleAssignment();
        instance2.setId(20L);
        instance2.setAsset(asset2);

        instance3 = new RuleAssignment();
        instance3.setId(30L);
        instance3.setAsset(asset2);

        alert1 = new Alert();
        alert1.setId(1L);
        alert1.setStatus("OPEN");
        alert1.setSeverity("HIGH");
        alert1.setNotificationCount(1);
        alert1.setCreatedAt(createdAt);
        alert1.setAcknowledgedAt(null);
        alert1.setLastNotifiedAt(createdAt);
        alert1.setResolvedAt(null);
        alert1.setRuleAssignment(instance1);

        alert2 = new Alert();
        alert2.setId(2L);
        alert2.setStatus("CLOSED");
        alert2.setSeverity("LOW");
        alert2.setNotificationCount(2);
        alert2.setCreatedAt(createdAt);
        alert2.setAcknowledgedAt(createdAt);
        alert2.setLastNotifiedAt(createdAt);
        alert2.setResolvedAt(LocalDateTime.of(2026, 9, 1, 12, 0));
        alert2.setRuleAssignment(instance2);

        alert3 = new Alert();
        alert3.setId(3L);
        alert3.setStatus("OPEN");
        alert3.setSeverity("HIGH");
        alert3.setNotificationCount(0);
        alert3.setCreatedAt(LocalDateTime.of(2026, 9, 10, 11, 0));
        alert3.setRuleAssignment(instance3);

    }

    @Test
    void returnsMappedAlertsWhenAlertsExist() {
        when(alertRepository.findAll()).thenReturn(List.of(alert1, alert2));

        List<AlertResponseDto> result = alertService.getAllAlerts();

        assertEquals(2, result.size());

        verify(alertRepository).findAll();
    }

    @Test
    void returnsEmptyListWhenNoAlertsExist() {
        when(alertRepository.findAll()).thenReturn(List.of());

        List<AlertResponseDto> result = alertService.getAllAlerts();

        assertTrue(result.isEmpty());

        verify(alertRepository).findAll();
    }

    @Test
    void returnsMappedAlertFilteredByStatus() {
        when(alertRepository.findByStatus("OPEN")).thenReturn(List.of(alert1));

        List<AlertResponseDto> result = alertService.getAlertsByStatus("OPEN");

        assertEquals(1, result.size());

        verify(alertRepository).findByStatus("OPEN");
    }

    @Test
    void returnsMappedAlertBasedOnId() {
        when(alertRepository.findById(1L)).thenReturn(Optional.of(alert1));

        AlertResponseDto response = alertService.getAlertById(1L);

        assertEquals(1L, response.getId());
        assertEquals(instance1.getId(), response.getRuleAssignmentId());
        assertEquals("OPEN", response.getStatus());

        verify(alertRepository, times(1)).findById(1L);

    }

    @Test
    void throwsExceptionWhenAlertDoesNotExist() {
        when(alertRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> alertService.getAlertById(99L));

        verify(alertRepository, times(1)).findById(99L);
    }

    @Test
    void updatesAlertWhenAcknowledgedIsCalled() {
        when(alertRepository.findById(1L)).thenReturn(Optional.of(alert1));

        AlertResponseDto response = alertService.acknowledgeAlert(alert1.getId());

        assertEquals("ACKNOWLEDGED", response.getStatus());
        assertNotNull(response.getAcknowledgedAt());

        verify(alertRepository).save(alert1);
        verify(alertRepository).findById(1L);
    }

    @Test
    void throwsExceptionWhenAcknowledgingMissingAlert() {
        when(alertRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> alertService.acknowledgeAlert(99L));

        verify(alertRepository).findById(99L);
        verify(alertRepository, never()).save(any());
    }

    @Test
    void resolvesAlertWhenAlertExists() {
        when(alertRepository.findById(1L)).thenReturn(Optional.of(alert1));

        AlertResponseDto response = alertService.resolveAlert(alert1.getId());

        assertEquals("RESOLVED", response.getStatus());
        assertNotNull(response.getResolvedAt());

        assertEquals("RESOLVED", alert1.getStatus());
        assertNotNull(alert1.getResolvedAt());

        verify(alertRepository).save(alert1);
        verify(alertRepository).findById(1L);
    }

    @Test
    void throwsExceptionWhenResolvingMissingAlert() {
        when(alertRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> alertService.resolveAlert(99L));

        verify(alertRepository).findById(99L);
        verify(alertRepository, never()).save(any());
    }

    @Test
    void returnsMappedAlertsBasedOnRuleAssignmentAssetId() {
        when(alertRepository.findByRuleAssignment_Asset_Id(2L)).thenReturn(List.of(alert2, alert3));

        List<AlertResponseDto> responses = alertService.getAlertByRuleAssignmentAssetId(2L);

        assertEquals(2, responses.size());
        assertEquals(2L, responses.get(0).getId());
        assertEquals(3L, responses.get(1).getId());

        verify(alertRepository).findByRuleAssignment_Asset_Id(2L);

    }

    @Test
    void returnsEmptyListWhenNoAlertsExistForAsset() {
        when(alertRepository.findByRuleAssignment_Asset_Id(99L)).thenReturn(List.of());

        List<AlertResponseDto> responses = alertService.getAlertByRuleAssignmentAssetId(99L);

        assertTrue(responses.isEmpty());

        verify(alertRepository).findByRuleAssignment_Asset_Id(99L);
    }

    @Test
    void throwsExceptionWhenAcknowledgingResolvedAlert() {
        alert1.setStatus("RESOLVED");
        alert1.setResolvedAt(LocalDateTime.of(2026, 9, 11, 10, 30));

        when(alertRepository.findById(1L)).thenReturn(Optional.of(alert1));

        assertThrows(InvalidAlertStateException.class,
                () -> alertService.acknowledgeAlert(1L));

        assertEquals("RESOLVED", alert1.getStatus());
        assertNotNull(alert1.getResolvedAt());

        verify(alertRepository).findById(1L);
        verify(alertRepository, never()).save(any());
    }

    @Test
    void throwsExceptionWhenResolvingResolvedAlert() {
        alert1.setStatus("RESOLVED");
        alert1.setResolvedAt(LocalDateTime.of(2026, 9, 11, 10, 30));

        when(alertRepository.findById(1L)).thenReturn(Optional.of(alert1));

        assertThrows(InvalidAlertStateException.class,
                () -> alertService.resolveAlert(1L));

        assertEquals("RESOLVED", alert1.getStatus());
        assertNotNull(alert1.getResolvedAt());

        verify(alertRepository).findById(1L);
        verify(alertRepository, never()).save(any());
    }

}
