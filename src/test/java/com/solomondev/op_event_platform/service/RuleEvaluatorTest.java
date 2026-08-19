package com.solomondev.op_event_platform.service;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import com.solomondev.op_event_platform.entity.enums.RuleOperator;

public class RuleEvaluatorTest {
    private final RuleEvaluator ruleEvaluator = new RuleEvaluator();

    // GREATER_THAN TESTS
    @Test
    void returnsTrueWhenEventValueIsGreaterThanThreshold() {

        // Expectation: True
        Assertions.assertTrue(
                ruleEvaluator.evaluate(new BigDecimal("101"),
                        RuleOperator.GREATER_THAN, new BigDecimal("100")));
    }

    @Test
    void returnsFalseWhenEventValueEqualsThresholdForGreaterThan() {
        // Expectation: False
        Assertions.assertFalse(
                ruleEvaluator.evaluate(new BigDecimal("100"),
                        RuleOperator.GREATER_THAN, new BigDecimal("100")));
    }

    @Test
    void returnsFalseWhenEventValueIsLessThanThresholdForGreaterThan() {
        // Expectation: False
        Assertions.assertFalse(
                ruleEvaluator.evaluate(new BigDecimal("99"),
                        RuleOperator.GREATER_THAN, new BigDecimal("100")));
    }

    // LESS_THAN TESTS
    @Test
    void returnsTrueWhenEventValueIsLessThanThreshold() {
        // Expectation: True
        Assertions.assertTrue(
                ruleEvaluator.evaluate(new BigDecimal("99"),
                        RuleOperator.LESS_THAN, new BigDecimal("100")));
    }

    @Test
    void returnsFalseWhenEventValueEqualsThresholdForLessThan() {
        // Expectation: False
        Assertions.assertFalse(
                ruleEvaluator.evaluate(new BigDecimal("100"),
                        RuleOperator.LESS_THAN, new BigDecimal("100")));
    }

    @Test
    void returnsFalseWhenEventValueIsGreaterThanThresholdForLessThan() {
        // Expectation: False
        Assertions.assertFalse(
                ruleEvaluator.evaluate(new BigDecimal("101"),
                        RuleOperator.LESS_THAN, new BigDecimal("100")));
    }

    // EQUALS TESTS
    @Test
    void returnsTrueWhenEventValueEqualsThreshold() {
        // Expectation: True
        Assertions.assertTrue(
                ruleEvaluator.evaluate(new BigDecimal("100"),
                        RuleOperator.EQUALS, new BigDecimal("100")));
    }

    @Test
    void returnsFalseWhenEventValueIsNotEqualToThreshold() {
        // Expectation: False
        Assertions.assertFalse(
                ruleEvaluator.evaluate(new BigDecimal("101"),
                        RuleOperator.EQUALS, new BigDecimal("100")));
    }

    // GREATER_THAN_OR_EQUAL TESTS
    @Test
    void returnsTrueWhenEventValueIsGreaterThanOrEqualToThreshold() {
        // Expectation: True
        Assertions.assertTrue(
                ruleEvaluator.evaluate(new BigDecimal("101"),
                        RuleOperator.GREATER_THAN_OR_EQUAL, new BigDecimal("100")));
    }

    @Test
    void returnsTrueWhenEventValueEqualsThresholdForGreaterThanOrEqualTo() {
        // Expectation: True
        Assertions.assertTrue(
                ruleEvaluator.evaluate(new BigDecimal("100"),
                        RuleOperator.GREATER_THAN_OR_EQUAL, new BigDecimal("100")));
    }

    @Test
    void returnsFalseWhenEventValueIsLessThanThresholdForGreaterThanOrEqualTo() {
        // Expectation: False
        Assertions.assertFalse(
                ruleEvaluator.evaluate(new BigDecimal("99"),
                        RuleOperator.GREATER_THAN_OR_EQUAL, new BigDecimal("100")));
    }

    // LESS_THAN_OR_EQUAL TESTS
    @Test
    void returnsTrueWhenEventValueIsLessThanOrEqualToThreshold() {
        // Expectation: True
        Assertions.assertTrue(
                ruleEvaluator.evaluate(new BigDecimal("99"),
                        RuleOperator.LESS_THAN_OR_EQUAL, new BigDecimal("100")));
    }

    @Test
    void returnsTrueWhenEventValueEqualsThresholdForLessThanOrEqualTo() {
        // Expectation: True
        Assertions.assertTrue(
                ruleEvaluator.evaluate(new BigDecimal("100"),
                        RuleOperator.LESS_THAN_OR_EQUAL, new BigDecimal("100")));
    }

    @Test
    void returnsFalseWhenEventValueIsGreaterThanThresholdForLessThanOrEqualTo() {
        // Expectation: False
        Assertions.assertFalse(
                ruleEvaluator.evaluate(new BigDecimal("101"),
                        RuleOperator.LESS_THAN_OR_EQUAL, new BigDecimal("100")));
    }

    // INVALID OPERATOR TESTS
    @Test
    void throwsIllegalArgumentExceptionWhenInvalidOperatorIsPassed() {
        // Expectation: IllegalArgumentException
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> ruleEvaluator.evaluate(new BigDecimal("100"),
                        null, new BigDecimal("100")));
    }
}
