package com.solomondev.op_event_platform.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import com.solomondev.op_event_platform.model.dto.AlertResponseDto;
import com.solomondev.op_event_platform.model.dto.PageResponse;
import com.solomondev.op_event_platform.model.exception.GlobalExceptionHandler;
import com.solomondev.op_event_platform.service.AlertService;

@WebMvcTest(AlertController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://issuer.example.test")
class AlertControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private AlertService alertService;
    @MockitoBean private JwtDecoder jwtDecoder;

    @Test
    void returnsPagedAlertsUsingFiltersAndDefaults() throws Exception {
        AlertResponseDto alert = new AlertResponseDto();
        alert.setId(7L);
        alert.setStatus("OPEN");
        when(alertService.searchAlerts(eq("OPEN"), eq("HIGH"), eq(2L), eq("payment"),
                isNull(), isNull(), eq(0), eq(20), eq(Sort.Direction.DESC)))
                .thenReturn(new PageResponse<>(List.of(alert), 0, 20, 1, 1));

        mockMvc.perform(get("/api/alerts")
                .param("status", "OPEN")
                .param("severity", "HIGH")
                .param("assetId", "2")
                .param("q", "payment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(7))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(alertService).searchAlerts("OPEN", "HIGH", 2L, "payment",
                null, null, 0, 20, Sort.Direction.DESC);
    }

    @Test
    void rejectsPageSizeAboveMaximum() throws Exception {
        mockMvc.perform(get("/api/alerts").param("size", "101"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(alertService);
    }

    @Test
    void returnsAnAlertById() throws Exception {
        AlertResponseDto alert = new AlertResponseDto();
        alert.setId(7L);
        when(alertService.getAlertById(7L)).thenReturn(alert);

        mockMvc.perform(get("/api/alerts/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));

        verify(alertService).getAlertById(7L);
    }
}
