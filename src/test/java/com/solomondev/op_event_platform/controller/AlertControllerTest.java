package com.solomondev.op_event_platform.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.solomondev.op_event_platform.model.dto.AlertResponseDto;
import com.solomondev.op_event_platform.model.exception.InvalidAlertStateException;
import com.solomondev.op_event_platform.model.exception.ResourceNotFoundException;
import com.solomondev.op_event_platform.service.AlertService;

@WebMvcTest(AlertController.class)
public class AlertControllerTest {
        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private AlertService alertService;

        private AlertResponseDto alert1;

        private AlertResponseDto alert2;

        @BeforeEach
        void setUp() {
                LocalDateTime createdAt = LocalDateTime.now();
                alert1 = new AlertResponseDto(
                                1L, 10L, "OPEN", "HIGH", 1,
                                createdAt, createdAt, createdAt, createdAt);

                alert2 = new AlertResponseDto(
                                2L, 20L, "OPEN", "MEDIUM", 0,
                                createdAt, createdAt, createdAt, createdAt);

        }

        @Test
        void returnsAlertsWhenAlertsExist() throws Exception {
                when(alertService.getAllAlerts()).thenReturn(List.of(alert1, alert2));

                mockMvc.perform(get("/api/alerts"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(2))
                                .andExpect(jsonPath("$[0].id").value(1))
                                .andExpect(jsonPath("$[0].status").value("OPEN"))
                                .andExpect(jsonPath("$[0].severity").value("HIGH"));

                verify(alertService, times(1)).getAllAlerts();
        }

        @Test
        void returnsEmptyListWhenNoAlertsExist() throws Exception {
                when(alertService.getAllAlerts()).thenReturn(List.of());

                mockMvc.perform(get("/api/alerts"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(0));

                verify(alertService, times(1)).getAllAlerts();
        }

        @Test
        void returnsAllAlertsWhenNoStatusParameterIsProvided() throws Exception {

                when(alertService.getAllAlerts()).thenReturn(List.of(alert1, alert2));

                mockMvc.perform(get("/api/alerts"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(2));

                verify(alertService, times(1)).getAllAlerts();

        }

        @Test
        void returnsMappedAlertWhenStatusIsProvided() throws Exception {
                when(alertService.getAlertsByStatus("OPEN")).thenReturn(List.of(alert1, alert2));

                mockMvc.perform(get("/api/alerts").param("status", "OPEN"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(2))
                                .andExpect(jsonPath("$[0].status").value("OPEN"));

                verify(alertService, times(1)).getAlertsByStatus("OPEN");
        }

        @Test
        void returnsMappedAlertById() throws Exception {
                when(alertService.getAlertById(1L)).thenReturn(alert1);

                mockMvc.perform(get("/api/alerts/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.ruleAssignmentId").value(10))
                                .andExpect(jsonPath("$.status").value("OPEN"));

                verify(alertService, times(1)).getAlertById(1L);
        }

        @Test
        void returnsExceptionWhenAlertDoesNotExist() throws Exception {
                when(alertService.getAlertById(99L))
                                .thenThrow(new ResourceNotFoundException(
                                                "Alert not found with id: " + 99L));

                mockMvc.perform(get("/api/alerts/99"))
                                .andExpect(status().isNotFound());

                verify(alertService).getAlertById(99L);
        }

        @Test
        void acknowledgesAlertWhenAlertExists() throws Exception {

                alert1.setStatus("ACKNOWLEDGED");
                alert1.setAcknowledgedAt(LocalDateTime.of(2026, 9, 9, 11, 30));

                when(alertService.acknowledgeAlert(1L)).thenReturn(alert1);

                mockMvc.perform(patch("/api/alerts/1/acknowledge"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.status").value("ACKNOWLEDGED"))
                                .andExpect(jsonPath("$.acknowledgedAt").exists());

                verify(alertService).acknowledgeAlert(1L);
        }

        @Test
        void returnsNotFoundWhenAcknowledgingMissingAlert() throws Exception {
                when(alertService.acknowledgeAlert(99L))
                                .thenThrow(new ResourceNotFoundException(
                                                "Alert not found with id: 99"));

                mockMvc.perform(patch("/api/alerts/99/acknowledge"))
                                .andExpect(status().isNotFound());

                verify(alertService).acknowledgeAlert(99L);
        }

        @Test
        void resolvesAlertWhenAlertExists() throws Exception {

                alert1.setStatus("RESOLVED");
                alert1.setResolvedAt(LocalDateTime.of(2026, 9, 9, 11, 30));

                when(alertService.resolveAlert(1L)).thenReturn(alert1);

                mockMvc.perform(patch("/api/alerts/1/resolve"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.status").value("RESOLVED"))
                                .andExpect(jsonPath("$.resolvedAt").exists());

                verify(alertService).resolveAlert(1L);
        }

        @Test
        void returnsNotFoundWhenResolvingMissingAlert() throws Exception {
                when(alertService.resolveAlert(99L))
                                .thenThrow(new ResourceNotFoundException(
                                                "Alert not found with id: 99"));

                mockMvc.perform(patch("/api/alerts/99/resolve"))
                                .andExpect(status().isNotFound());

                verify(alertService).resolveAlert(99L);
        }

        @Test
        void returnsAlertsByAssetId() throws Exception {
                // Tell the mocked service what to return when the controller asks for
                // alerts associated with asset ID 2.
                when(alertService.getAlertByRuleAssignmentAssetId(2L))
                                .thenReturn(List.of(alert1, alert2));

                // Send a simulated HTTP GET request to the controller with assetId=2.
                mockMvc.perform(get("/api/alerts?assetId=2"))

                                // Verify that the endpoint returns HTTP 200 OK.
                                .andExpect(status().isOk())

                                // Verify that the first alert in the JSON response has ID 1.
                                .andExpect(jsonPath("$[0].id").value(1))

                                // Verify that the second alert in the JSON response has ID 2.
                                .andExpect(jsonPath("$[1].id").value(2));

                // Verify that the controller delegated to the correct service method
                // and passed the query parameter value as Long 2L.
                verify(alertService).getAlertByRuleAssignmentAssetId(2L);
        }

        @Test
        void returnsBadRequestWhenAcknowledgingResolvedAlert() throws Exception {

                when(alertService.acknowledgeAlert(1L))
                                .thenThrow(new InvalidAlertStateException(
                                                "Resolved alerts cannot be acknowledged"));

                mockMvc.perform(patch("/api/alerts/1/acknowledge"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.message").value("Resolved alerts cannot be acknowledged"));

                verify(alertService).acknowledgeAlert(1L);
        }
}
