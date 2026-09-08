package com.solomondev.op_event_platform.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.solomondev.op_event_platform.entity.Event;
import com.solomondev.op_event_platform.model.dto.CreateEventRequestDto;
import com.solomondev.op_event_platform.service.EventIngestionService;

import jakarta.validation.Valid;
@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventIngestionService eventIngestionService;

    public EventController(EventIngestionService eventIngestionService) {
        this.eventIngestionService = eventIngestionService;
    }

    @PostMapping
    public ResponseEntity<Event> createEvent(@Valid @RequestBody CreateEventRequestDto request) {
        Event savedEvent = eventIngestionService.ingestEvent(request);

        return new ResponseEntity<>(savedEvent, HttpStatus.CREATED);
    }

}
