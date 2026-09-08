package com.solomondev.op_event_platform.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}
