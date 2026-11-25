package com.solomondev.op_event_platform.controller;

import java.net.URI;
import java.time.LocalDateTime;

import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.solomondev.op_event_platform.entity.Event;
import com.solomondev.op_event_platform.model.dto.CreateEventRequestDto;
import com.solomondev.op_event_platform.model.dto.EventResponseDto;
import com.solomondev.op_event_platform.model.dto.PageResponse;
import com.solomondev.op_event_platform.service.EventIngestionService;
import com.solomondev.op_event_platform.service.EventQueryService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Validated
@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventIngestionService eventIngestionService;
    private final EventQueryService eventQueryService;

    public EventController(EventIngestionService eventIngestionService, EventQueryService eventQueryService) {
        this.eventIngestionService = eventIngestionService;
        this.eventQueryService = eventQueryService;
    }

    @PostMapping
    public ResponseEntity<EventResponseDto> createEvent(@Valid @RequestBody CreateEventRequestDto request) {
        Event savedEvent = eventIngestionService.ingestEvent(request);
        EventResponseDto response = eventQueryService.getEventById(savedEvent.getId());
        return ResponseEntity.created(URI.create("/api/events/" + savedEvent.getId())).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<EventResponseDto>> searchEvents(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        return ResponseEntity.ok(eventQueryService.searchEvents(
                q, status, severity, from, to, page, size, direction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDto> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventQueryService.getEventById(id));
    }
}
