package com.solomondev.op_event_platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.solomondev.op_event_platform.entity.Alert;
import com.solomondev.op_event_platform.entity.Asset;
import com.solomondev.op_event_platform.entity.RuleAssignment;
import com.solomondev.op_event_platform.model.dto.AlertResponseDto;
import com.solomondev.op_event_platform.model.dto.PageResponse;
import com.solomondev.op_event_platform.model.exception.ResourceNotFoundException;
import com.solomondev.op_event_platform.repository.AlertEventRepository;
import com.solomondev.op_event_platform.repository.AlertRepository;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {
    @Mock private AlertRepository alertRepository;
    @Mock private AlertEventRepository alertEventRepository;
    @Mock private CurrentUserService currentUserService;
    @InjectMocks private AlertService alertService;

    private Alert alert;

    @BeforeEach
    void setUp() {
        Asset asset = new Asset();
        asset.setId(4L);
        asset.setName("Payments API");
        RuleAssignment assignment = new RuleAssignment();
        assignment.setId(8L);
        assignment.setAsset(asset);
        alert = new Alert();
        alert.setId(12L);
        alert.setRuleAssignment(assignment);
        alert.setStatus("OPEN");
        alert.setSeverity("HIGH");
        alert.setNotificationCount(0);
        when(currentUserService.requireSubject()).thenReturn("oidc-subject");
    }

    @Test
    void searchesOnlyCurrentUsersTenantAndMapsAlertPage() {
        when(alertEventRepository.findAllByAlert_IdIn(anyList())).thenReturn(List.of());
        when(alertRepository.searchAccessible(eq("oidc-subject"), isNull(), isNull(), isNull(),
                eq(""), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(alert), PageRequest.of(0, 20), 1));

        PageResponse<AlertResponseDto> result =
                alertService.searchAlerts(null, null, null, null, null, null, 0, 20, Sort.Direction.DESC);

        assertEquals(1, result.totalElements());
        assertEquals(12L, result.content().get(0).getId());
        assertEquals(4L, result.content().get(0).getAssetId());
        verify(alertRepository).searchAccessible(eq("oidc-subject"), isNull(), isNull(), isNull(),
                eq(""), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class));
    }

    @Test
    void hidesAlertsOutsideCurrentUsersTenant() {
        when(alertRepository.findAccessibleById(99L, "oidc-subject")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> alertService.getAlertById(99L));
        verify(alertRepository).findAccessibleById(99L, "oidc-subject");
    }

    @Test
    void acknowledgesAccessibleAlert() {
        when(alertEventRepository.findAllByAlert_IdIn(anyList())).thenReturn(List.of());
        when(alertRepository.findAccessibleById(12L, "oidc-subject")).thenReturn(Optional.of(alert));
        when(alertRepository.save(alert)).thenReturn(alert);

        AlertResponseDto result = alertService.acknowledgeAlert(12L);

        assertEquals("ACKNOWLEDGED", result.getStatus());
        assertNotNull(result.getAcknowledgedAt());
        verify(alertRepository).save(alert);
    }
}
