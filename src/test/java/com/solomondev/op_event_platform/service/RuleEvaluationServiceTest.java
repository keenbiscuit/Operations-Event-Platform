package com.solomondev.op_event_platform.service;

import java.math.BigDecimal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.solomondev.op_event_platform.entity.Asset;
import com.solomondev.op_event_platform.entity.Event;
import com.solomondev.op_event_platform.entity.Rule;
import com.solomondev.op_event_platform.entity.RuleAssignment;
import com.solomondev.op_event_platform.entity.enums.RuleOperator;

public class RuleEvaluationServiceTest {
    private RuleEvaluationService ruleEvaluationService;
    private Event event;
    private Asset asset;
    private RuleAssignment assignment;

    @BeforeEach
    void setUp() {
        RuleEvaluator ruleEvaluator = new RuleEvaluator();
        ruleEvaluationService = new RuleEvaluationService(ruleEvaluator);

        // Asset instance
        asset = new Asset();
        asset.setId(1L);

        // Event instance
        event = new Event();
        event.setAsset(asset);
        event.setEventType("PAYMENT");
        event.setValue(new BigDecimal("150"));

        // Rule instance
        Rule rule = new Rule();
        rule.setConditionType("PAYMENT");
        rule.setOperator(RuleOperator.GREATER_THAN);

        // RuleAssignment instance
        assignment = new RuleAssignment();
        assignment.setRule(rule);
        assignment.setAsset(asset);
        assignment.setThreshold(new BigDecimal("100"));
        assignment.setEnabled(true);
        assignment.setSeverity("HIGH");

    }

    @Test
    public void returnsTrueWhenEnabledAssignmentMatchesEvent() {

        Assertions.assertTrue(ruleEvaluationService.matches(event, assignment));
    }

    @Test
    public void returnsFalseWhenAssignmentIsDisabled() {
        assignment.setEnabled(false);
        Assertions.assertFalse(ruleEvaluationService.matches(event, assignment));
    }

    @Test
    public void returnsFalseWhenEventAssetDoesNotMatchAssignmentAsset() {
        Asset secondAsset = new Asset();
        asset.setId(2L);
        assignment.setAsset(secondAsset);
        Assertions.assertFalse(ruleEvaluationService.matches(event, assignment));
    }

    @Test
    public void returnFalseWhenTypesDontMatch() {
        event.setEventType("LOGIN");
        Assertions.assertFalse(ruleEvaluationService.matches(event, assignment));
    }

    @Test
    public void returnsFalseWhenThresholdNotMet() {
        event.setValue(new BigDecimal("100"));
        Assertions.assertFalse(ruleEvaluationService.matches(event, assignment));
    }

    @Test
    public void returnsFalseWhenEventIsNull() {
        Assertions.assertFalse(ruleEvaluationService.matches(null, assignment));
    }

    @Test
    public void returnsFalseWhenAssignmentIsNull() {
        Assertions.assertFalse(ruleEvaluationService.matches(event, null));
    }
}
