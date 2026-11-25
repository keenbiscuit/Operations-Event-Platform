package com.solomondev.op_event_platform.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import com.solomondev.op_event_platform.entity.Event;
import com.solomondev.op_event_platform.model.dto.CreateEventRequestDto;
import com.solomondev.op_event_platform.model.dto.EventResponseDto;
import com.solomondev.op_event_platform.model.dto.PageResponse;
import com.solomondev.op_event_platform.model.exception.GlobalExceptionHandler;
import com.solomondev.op_event_platform.model.exception.ResourceNotFoundException;
import com.solomondev.op_event_platform.service.EventIngestionService;
import com.solomondev.op_event_platform.service.EventQueryService;

@WebMvcTest(EventController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@TestPropertySource(properties = "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://issuer.example.test")
class EventControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private EventIngestionService eventIngestionService;
    @MockitoBean private EventQueryService eventQueryService;
    @MockitoBean private JwtDecoder jwtDecoder;

    private EventResponseDto response() {
        return new EventResponseDto(1L, 2L, "Payments API", "PAYMENT",
                new BigDecimal("150"), LocalDateTime.of(2026, 8, 28, 10, 0), List.of());
    }

    @Test
    void createsEventAndReturnsLocationAndRepresentation() throws Exception {
        Event event = new Event();
        event.setId(1L);
        when(eventIngestionService.ingestEvent(any(CreateEventRequestDto.class))).thenReturn(event);
        when(eventQueryService.getEventById(1L)).thenReturn(response());

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"assetId":2,"eventType":"PAYMENT","value":150}
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/events/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.assetName").value("Payments API"));

        verify(eventIngestionService).ingestEvent(any(CreateEventRequestDto.class));
        verify(eventQueryService).getEventById(1L);
    }

    @Test
    void rejectsInvalidEventRequestBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"eventType":" ","value":150}
                        """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(eventIngestionService, eventQueryService);
    }

    @Test
    void listsEventsWithPagedResponse() throws Exception {
        when(eventQueryService.searchEvents(eq("payment"), isNull(), isNull(),
                isNull(), isNull(), eq(0), eq(20), any()))
                .thenReturn(new PageResponse<>(List.of(response()), 0, 20, 1, 1));

        mockMvc.perform(get("/api/events").param("q", "payment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(eventQueryService).searchEvents(eq("payment"), isNull(), isNull(),
                isNull(), isNull(), eq(0), eq(20), any());
    }

    @Test
    void returnsNotFoundForUnknownEvent() throws Exception {
        when(eventQueryService.getEventById(999L))
                .thenThrow(new ResourceNotFoundException("Event not found with id: 999"));

        mockMvc.perform(get("/api/events/999")).andExpect(status().isNotFound());
        verify(eventQueryService).getEventById(999L);
    }
}
