package com.solomondev.op_event_platform.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.solomondev.op_event_platform.entity.Event;
import com.solomondev.op_event_platform.model.CreateEventRequestDto;
import com.solomondev.op_event_platform.model.exception.GlobalExceptionHandler;
import com.solomondev.op_event_platform.model.exception.ResourceNotFoundException;
import com.solomondev.op_event_platform.service.EventIngestionService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
@Import(GlobalExceptionHandler.class)
public class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventIngestionService eventIngestionService;

    private CreateEventRequestDto dto;

    private Event event;

    @BeforeEach
    void setUp() {

        dto = new CreateEventRequestDto();
        dto.setAssetId(1L);
        dto.setEventType("PAYMENT");
        dto.setValue(new BigDecimal("150"));

        event = new Event();
        event.setId(1L);
        event.setEventType("PAYMENT");
        event.setValue(new BigDecimal("150"));
        event.setOccurredAt(LocalDateTime.of(2026, 8, 28, 10, 0));
    }

    @Test
    void createsEventWhenRequestIsValid() throws Exception {
        when(eventIngestionService.ingestEvent(any(CreateEventRequestDto.class))).thenReturn(event);

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                {
                                  "assetId": 1,
                                  "eventType": "PAYMENT",
                                  "value": 150
                                }
                                """))
                .andExpect(status().isCreated());

        verify(eventIngestionService, times(1))
                .ingestEvent(any(CreateEventRequestDto.class));

    }

    @Test
    void missingAssetIdThrowsBadRequest() throws Exception {

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                {
                                  "eventType": "PAYMENT",
                                  "value": 150
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(eventIngestionService);

    }

    @Test
    void blankEventTypeThrowsBadRequest() throws Exception {
        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                {
                                  "assetId": 1,
                                  "eventType": " ",
                                  "value": 150
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(eventIngestionService);
    }

    @Test
    void missingAssetThrowsNotFoundException() throws Exception {
        when(eventIngestionService.ingestEvent(any(CreateEventRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("Asset not found with id: 999"));

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                {
                                  "assetId": 999,
                                  "eventType": "PAYMENT",
                                  "value": 150
                                }
                                """))
                .andExpect(status().isNotFound());

        verify(eventIngestionService, times(1))
                .ingestEvent(any(CreateEventRequestDto.class));
    }
}
