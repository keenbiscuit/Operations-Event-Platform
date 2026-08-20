/*Load event
-> find assignments for its asset
-> evaluate every assignment
-> for each match, save Alert
-> link the Alert to the Event with AlertEvent */

package com.solomondev.op_event_platform.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.solomondev.op_event_platform.entity.Alert;
import com.solomondev.op_event_platform.entity.AlertEvent;
import com.solomondev.op_event_platform.entity.Event;
import com.solomondev.op_event_platform.entity.RuleAssignment;
import com.solomondev.op_event_platform.repository.AlertEventRepository;
import com.solomondev.op_event_platform.repository.AlertRepository;
import com.solomondev.op_event_platform.repository.EventRepository;
import com.solomondev.op_event_platform.repository.RuleAssignmentRepository;

@Service
public class EventProcessingService {

    private final EventRepository eventRepository;
    private final RuleAssignmentRepository ruleAssignmentRepository;
    private final AlertRepository alertRepository;
    private final AlertEventRepository alertEventRepository;
    private final RuleEvaluationService ruleEvaluationService;

    public EventProcessingService(EventRepository eventRepository,
            AlertRepository alertRepository,
            AlertEventRepository alertEventRepository,
            RuleAssignmentRepository ruleAssignmentRepository,
            RuleEvaluationService ruleEvaluationService) {

        this.eventRepository = eventRepository;
        this.ruleAssignmentRepository = ruleAssignmentRepository;
        this.alertRepository = alertRepository;
        this.alertEventRepository = alertEventRepository;
        this.ruleEvaluationService = ruleEvaluationService;

    }

    @Transactional
    public void processEvent(Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        // Find assignments
        List<RuleAssignment> assignments = ruleAssignmentRepository.findByAsset_Id(event.getAsset().getId());

        // Loop through assignments
        for (RuleAssignment assignment : assignments) {
            if (ruleEvaluationService.matches(event, assignment)) {
                // Create Timestamp
                LocalDateTime now = LocalDateTime.now();

                // Create new Alert
                Alert alert = new Alert();
                alert.setRuleAssignment(assignment);
                alert.setStatus("OPEN");
                alert.setSeverity(assignment.getSeverity());
                alert.setNotificationCount(0);
                alert.setCreatedAt(now);

                // Save the Alert
                Alert savedAlert = alertRepository.save(alert);

                // Create new AlertEvent
                AlertEvent alertEvent = new AlertEvent();
                alertEvent.setAlert(savedAlert);
                alertEvent.setEvent(event);
                alertEvent.setLinkedAt(now);

                // Save to alertEventRepo
                alertEventRepository.save(alertEvent);
            }
        }
    }

}
