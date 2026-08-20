package com.solomondev.op_event_platform.service;

import org.springframework.stereotype.Service;

import com.solomondev.op_event_platform.entity.Event;
import com.solomondev.op_event_platform.entity.RuleAssignment;

@Service
public class RuleEvaluationService {

    private final RuleEvaluator ruleEvaluator;

    public RuleEvaluationService(RuleEvaluator ruleEvaluator) {
        this.ruleEvaluator = ruleEvaluator;
    }

    public boolean matches(Event event, RuleAssignment assignment) {
        // Reject null event or null assignment
        if (event == null || assignment == null)
            return false;

        /*
         * When assignment is disabled false
         * Since enabled is a Boolean if we use == a nullptr
         * exception could be thrown when java tries to unbox it
         */
        if (!Boolean.TRUE.equals(assignment.getEnabled()))
            return false;

        // When event asset doesn't match an assignment asset false
        else if (!event.getAsset().getId().equals(assignment.getAsset().getId()))
            return false;

        // When event type and condition type differ false
        else if (!event.getEventType().equals(assignment.getRule().getConditionType()))
            return false;

        // Otherwise call rule evaluator using event value, rule operator and assignment
        // threshold
        return ruleEvaluator.evaluate(event.getValue(),
                assignment.getRule().getOperator(),
                assignment.getThreshold());
    }
}
