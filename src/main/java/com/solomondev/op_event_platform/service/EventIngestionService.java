package com.solomondev.op_event_platform.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.solomondev.op_event_platform.entity.Asset;
import com.solomondev.op_event_platform.entity.Event;
import com.solomondev.op_event_platform.model.dto.CreateEventRequestDto;
import com.solomondev.op_event_platform.model.exception.ResourceNotFoundException;
import com.solomondev.op_event_platform.repository.AssetRepository;
import com.solomondev.op_event_platform.repository.EventRepository;

import jakarta.transaction.Transactional;

@Service
public class EventIngestionService {
    private AssetRepository assetRepository;
    private EventRepository eventRepository;
    private EventProcessingService eventProcessingService;

    public EventIngestionService(AssetRepository assetRepository, EventRepository eventRepository,
            EventProcessingService eventProcessingService) {
        this.assetRepository = assetRepository;
        this.eventRepository = eventRepository;
        this.eventProcessingService = eventProcessingService;
    }

    /*
     * Need transactional because saving the event and processing its alert workflow
     * represent one business transaction
     */
    @Transactional
    public Event ingestEvent(CreateEventRequestDto request) {
        // Load existing asset
        Asset asset = assetRepository.findById(request.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + request.getAssetId()));

        // Build an event
        Event event = new Event();
        event.setAsset(asset);
        event.setEventType(request.getEventType());
        event.setValue(request.getValue());
        event.setOccurredAt(LocalDateTime.now());

        // Save event
        Event savedEvent = eventRepository.save(event);

        // Process saved eventId
        eventProcessingService.processEvent(savedEvent.getId());
        return savedEvent;

    }
}
