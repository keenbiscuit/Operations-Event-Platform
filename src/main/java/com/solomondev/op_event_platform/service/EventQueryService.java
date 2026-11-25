package com.solomondev.op_event_platform.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.solomondev.op_event_platform.entity.AlertEvent;
import com.solomondev.op_event_platform.entity.Event;
import com.solomondev.op_event_platform.model.dto.EventAlertSummaryDto;
import com.solomondev.op_event_platform.model.dto.EventResponseDto;
import com.solomondev.op_event_platform.model.dto.PageResponse;
import com.solomondev.op_event_platform.model.exception.ResourceNotFoundException;
import com.solomondev.op_event_platform.repository.AlertEventRepository;
import com.solomondev.op_event_platform.repository.EventRepository;

@Service
public class EventQueryService {
    private final EventRepository eventRepository;
    private final AlertEventRepository alertEventRepository;
    private final CurrentUserService currentUserService;

    public EventQueryService(
            EventRepository eventRepository,
            AlertEventRepository alertEventRepository,
            CurrentUserService currentUserService) {
        this.eventRepository = eventRepository;
        this.alertEventRepository = alertEventRepository;
        this.currentUserService = currentUserService;
    }

    public PageResponse<EventResponseDto> searchEvents(
            String query,
            String status,
            String severity,
            LocalDateTime fromTime,
            LocalDateTime toTime,
            int page,
            int size,
            Sort.Direction direction) {
        Page<Event> events = eventRepository.searchAccessible(
                currentUserService.requireSubject(),
                blankToEmpty(query),
                blankToNull(status),
                blankToNull(severity),
                fromBound(fromTime),
                toBound(toTime),
                PageRequest.of(page, Math.min(size, 100),
                        Sort.by(direction, "occurredAt").and(Sort.by(direction, "id"))));

        Map<Long, List<EventAlertSummaryDto>> alertsByEvent = loadAlertSummaries(events.getContent());
        return PageResponse.from(events.map(event -> mapToResponseDto(
                event, alertsByEvent.getOrDefault(event.getId(), List.of()))));
    }

    public EventResponseDto getEventById(Long id) {
        Event event = eventRepository.findAccessibleById(id, currentUserService.requireSubject())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        return mapToResponseDto(event, loadAlertSummaries(List.of(event))
                .getOrDefault(event.getId(), List.of()));
    }

    private Map<Long, List<EventAlertSummaryDto>> loadAlertSummaries(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }

        List<Long> eventIds = events.stream().map(Event::getId).toList();
        return alertEventRepository.findAllByEvent_IdIn(eventIds).stream()
                .collect(Collectors.groupingBy(
                        link -> link.getEvent().getId(),
                        Collectors.mapping(this::mapAlertSummary, Collectors.toList())));
    }

    private EventAlertSummaryDto mapAlertSummary(AlertEvent link) {
        return new EventAlertSummaryDto(
                link.getAlert().getId(),
                link.getAlert().getStatus(),
                link.getAlert().getSeverity(),
                link.getAlert().getCreatedAt());
    }

    private EventResponseDto mapToResponseDto(Event event, List<EventAlertSummaryDto> alerts) {
        return new EventResponseDto(
                event.getId(),
                event.getAsset().getId(),
                event.getAsset().getName(),
                event.getEventType(),
                event.getValue(),
                event.getOccurredAt(),
                alerts);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private LocalDateTime fromBound(LocalDateTime value) {
        return value == null ? LocalDateTime.of(1, 1, 1, 0, 0) : value;
    }

    private LocalDateTime toBound(LocalDateTime value) {
        return value == null ? LocalDateTime.of(9999, 12, 31, 23, 59, 59, 999_999_999) : value;
    }

    private String blankToEmpty(String value) {
        return value == null || value.isBlank() ? "" : value.trim();
    }
}

