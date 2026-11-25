package com.solomondev.op_event_platform.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solomondev.op_event_platform.entity.Asset;
import com.solomondev.op_event_platform.entity.Event;
import com.solomondev.op_event_platform.model.dto.CreateEventRequestDto;
import com.solomondev.op_event_platform.model.exception.ResourceNotFoundException;
import com.solomondev.op_event_platform.repository.AssetRepository;
import com.solomondev.op_event_platform.repository.EventRepository;
import com.solomondev.op_event_platform.repository.OrgMembershipRepository;

@Service
public class EventIngestionService {
    private final AssetRepository assetRepository;
    private final EventRepository eventRepository;
    private final EventProcessingService eventProcessingService;
    private final OrgMembershipRepository orgMembershipRepository;
    private final CurrentUserService currentUserService;

    public EventIngestionService(
            AssetRepository assetRepository,
            EventRepository eventRepository,
            EventProcessingService eventProcessingService,
            OrgMembershipRepository orgMembershipRepository,
            CurrentUserService currentUserService) {
        this.assetRepository = assetRepository;
        this.eventRepository = eventRepository;
        this.eventProcessingService = eventProcessingService;
        this.orgMembershipRepository = orgMembershipRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public Event ingestEvent(CreateEventRequestDto request) {
        String subject = currentUserService.requireSubject();
        Asset asset = assetRepository.findById(request.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + request.getAssetId()));

        boolean isMember = orgMembershipRepository.existsByOrg_IdAndUser_ExternalSubject(
                asset.getOrg().getId(), subject);
        if (!isMember) {
            // Use the same response for missing and inaccessible assets to avoid ID enumeration.
            throw new ResourceNotFoundException("Asset not found: " + request.getAssetId());
        }

        Event event = new Event();
        event.setAsset(asset);
        event.setEventType(request.getEventType());
        event.setValue(request.getValue());
        event.setOccurredAt(LocalDateTime.now());

        Event savedEvent = eventRepository.save(event);
        eventProcessingService.processEvent(savedEvent.getId());
        return savedEvent;
    }
}
