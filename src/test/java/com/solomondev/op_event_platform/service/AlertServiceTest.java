package com.solomondev.op_event_platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import com.solomondev.op_event_platform.model.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class AlertServiceTest {
    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private AlertService alertService;

    private Alert alert1;

    private Alert alert2;

    private RuleAssignment instance1;
    private RuleAssignment instance2;

    @BeforeEach
    void setUp() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 9, 7, 14, 0);

        instance1 = new RuleAssignment();
        instance2 = new RuleAssignment();
        instance1.setId(10L);
        instance2.setId(20L);

        alert1 = new Alert();
        alert2 = new Alert();

        alert1.setId(1L);
        alert1.setStatus("OPEN");
        alert1.setSeverity("HIGH");
        alert1.setNotificationCount(1);
        alert1.setCreatedAt(createdAt);
        alert1.setAcknowledgedAt(null);
        alert1.setLastNotifiedAt(createdAt);
        alert1.setResolvedAt(null);

        alert2.setId(2L);
        alert2.setStatus("CLOSED");
        alert2.setSeverity("LOW");
        alert2.setNotificationCount(2);
        alert2.setCreatedAt(createdAt);
        alert2.setAcknowledgedAt(createdAt);
        alert2.setLastNotifiedAt(createdAt);
        alert2.setResolvedAt(LocalDateTime.of(2026, 9, 1, 12, 0));

        alert1.setRuleAssignment(instance1);
        alert2.setRuleAssignment(instance2);

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

}
