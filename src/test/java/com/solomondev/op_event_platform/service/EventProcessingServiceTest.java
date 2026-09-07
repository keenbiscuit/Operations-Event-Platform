package com.solomondev.op_event_platform.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.solomondev.op_event_platform.entity.Alert;
import com.solomondev.op_event_platform.entity.AlertEvent;
import com.solomondev.op_event_platform.entity.Asset;
import com.solomondev.op_event_platform.entity.Event;
import com.solomondev.op_event_platform.entity.Rule;
import com.solomondev.op_event_platform.entity.RuleAssignment;
import com.solomondev.op_event_platform.entity.enums.RuleOperator;
import com.solomondev.op_event_platform.repository.AlertEventRepository;
import com.solomondev.op_event_platform.repository.AlertRepository;
import com.solomondev.op_event_platform.repository.EventRepository;
import com.solomondev.op_event_platform.repository.RuleAssignmentRepository;

@ExtendWith(MockitoExtension.class)
class EventProcessingServiceTest {
        @Mock
        private EventRepository eventRepository;

        @Mock
        private RuleAssignmentRepository ruleAssignmentRepository;

        @Mock
        private AlertRepository alertRepository;

        @Mock
        private AlertEventRepository alertEventRepository;

        @Mock
        private RuleEvaluationService ruleEvaluationService;

        @InjectMocks
        private EventProcessingService eventProcessingService;

        private Asset asset;
        private Event event;
        private Rule rule;
        private RuleAssignment assignment;

        @BeforeEach
        void setUp() {
                asset = new Asset();
                asset.setId(1L);

                event = new Event();
                event.setId(10L);
                event.setAsset(asset);
                event.setEventType("PAYMENT");
                event.setValue(new BigDecimal("150"));

                rule = new Rule();
                rule.setId(20L);
                rule.setConditionType("PAYMENT");
                rule.setOperator(RuleOperator.GREATER_THAN);

                assignment = new RuleAssignment();
                assignment.setId(30L);
                assignment.setAsset(asset);
                assignment.setRule(rule);
                assignment.setThreshold(new BigDecimal("100"));
                assignment.setSeverity("HIGH");
                assignment.setEnabled(true);
        }

        @Test
        void savesAlertAndAlertEventWhenAssignmentMatches() {
                // Arrange: configure test data and fake dependency behavior
                when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

                when(ruleAssignmentRepository.findByAsset_Id(1L))
                                .thenReturn(List.of(assignment));

                when(ruleEvaluationService.matches(event, assignment))
                                .thenReturn(true);

                when(alertRepository.save(any(Alert.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // Running service method
                eventProcessingService.processEvent(10L);

                // Confirm the expected writes
                verify(alertRepository, times(1)).save(any(Alert.class));
                verify(alertEventRepository, times(1)).save(any(AlertEvent.class));
        }

        @Test
        void savesTwoAlertsWhenTwoAssignmentsMatch() {
                RuleAssignment secondAssignment = new RuleAssignment();
                secondAssignment.setId(31L);
                secondAssignment.setAsset(asset);
                secondAssignment.setRule(rule);
                secondAssignment.setThreshold(new BigDecimal("100"));
                secondAssignment.setSeverity("MEDIUM");
                secondAssignment.setEnabled(true);

                when(eventRepository.findById(10L))
                                .thenReturn(Optional.of(event));

                when(ruleAssignmentRepository.findByAsset_Id(1L))
                                .thenReturn(List.of(assignment, secondAssignment));

                when(ruleEvaluationService.matches(event, assignment))
                                .thenReturn(true);

                when(ruleEvaluationService.matches(event, secondAssignment))
                                .thenReturn(true);

                when(alertRepository.save(any(Alert.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                eventProcessingService.processEvent(10L);

                verify(alertRepository, times(2)).save(any(Alert.class));
                verify(alertEventRepository, times(2)).save(any(AlertEvent.class));
        }

        @Test
        void doesNotSaveAlertWhenAssignmentDoesNotMatch() {
                when(eventRepository.findById(10L))
                                .thenReturn(Optional.of(event));

                when(ruleAssignmentRepository.findByAsset_Id(1L))
                                .thenReturn(List.of(assignment));

                when(ruleEvaluationService.matches(event, assignment))
                                .thenReturn(false);

                eventProcessingService.processEvent(10L);

                verify(alertRepository, never()).save(any(Alert.class));
                verify(alertEventRepository, never()).save(any(AlertEvent.class));
        }

        @Test
        void throwsExceptionWhenEventNotFound() {
                when(eventRepository.findById(10L))
                                .thenReturn(Optional.empty());

                Assertions.assertThrows(
                                IllegalArgumentException.class,
                                () -> eventProcessingService.processEvent(10L));

                verify(ruleAssignmentRepository, never()).findByAsset_Id(anyLong());
                verify(alertRepository, never()).save(any(Alert.class));
                verify(alertEventRepository, never()).save(any(AlertEvent.class));
        }
}
