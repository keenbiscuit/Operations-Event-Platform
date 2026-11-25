package com.solomondev.op_event_platform.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solomondev.op_event_platform.model.dto.DashboardSummaryDto;
import com.solomondev.op_event_platform.repository.AlertRepository;
import com.solomondev.op_event_platform.repository.EventRepository;

@Service
public class DashboardService {
    private final EventRepository eventRepository;
    private final AlertRepository alertRepository;
    private final CurrentUserService currentUserService;

    public DashboardService(
            EventRepository eventRepository,
            AlertRepository alertRepository,
            CurrentUserService currentUserService) {
        this.eventRepository = eventRepository;
        this.alertRepository = alertRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDto getSummary() {
        String subject = currentUserService.requireSubject();
        return new DashboardSummaryDto(
                eventRepository.countAccessible(subject),
                alertRepository.countAccessibleByStatus(subject, "OPEN"),
                alertRepository.countAccessibleByStatus(subject, "ACKNOWLEDGED"),
                alertRepository.countAccessibleByStatus(subject, "RESOLVED"));
    }
}
