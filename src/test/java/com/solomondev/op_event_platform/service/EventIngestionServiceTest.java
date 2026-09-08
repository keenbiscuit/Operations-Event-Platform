package com.solomondev.op_event_platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.solomondev.op_event_platform.entity.Asset;
import com.solomondev.op_event_platform.entity.Event;
import com.solomondev.op_event_platform.model.dto.CreateEventRequestDto;
import com.solomondev.op_event_platform.model.exception.ResourceNotFoundException;
import com.solomondev.op_event_platform.repository.AssetRepository;
import com.solomondev.op_event_platform.repository.EventRepository;

@ExtendWith(MockitoExtension.class)
public class EventIngestionServiceTest {
    @Mock
    private EventRepository eventRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private EventProcessingService eventProcessingService;

    @InjectMocks
    private EventIngestionService eventIngestionService;

    private Asset asset;

    private CreateEventRequestDto request;

    @BeforeEach
    void setUp() {
        asset = new Asset();
        asset.setId(1L);

        request = new CreateEventRequestDto(1L, "PAYMENT", new BigDecimal("150"));

    }

    @Test
    void savedEventAndProcessesSavedEvent() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));

        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event savedEvent = invocation.getArgument(0);
            savedEvent.setId(10L);
            return savedEvent;
        });

        Event result = eventIngestionService.ingestEvent(request);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);

        verify(eventRepository).save(eventCaptor.capture());
        verify(eventProcessingService).processEvent(10L);

        Event capturedEvent = eventCaptor.getValue();

        assertEquals(asset, capturedEvent.getAsset());
        assertEquals("PAYMENT", capturedEvent.getEventType());
        assertEquals(new BigDecimal("150"), capturedEvent.getValue());
        assertNotNull(capturedEvent.getOccurredAt());
        assertEquals(10L, result.getId());
    }

    @Test
    void throwsExceptionWhenAssetDoesNotExist() {
        when(assetRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> eventIngestionService.ingestEvent(request));

        assertEquals("Asset not found: 1", exception.getMessage());

        verify(eventRepository, never()).save(any(Event.class));
        verify(eventProcessingService, never()).processEvent(anyLong());
    }
}
