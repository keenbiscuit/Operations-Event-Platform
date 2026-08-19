package com.solomondev.op_event_platform.service;

import java.math.BigDecimal;
import com.solomondev.op_event_platform.entity.enums.RuleOperator;

public class RuleEvaluator {
    public boolean evaluate(BigDecimal eventValue, RuleOperator operator, BigDecimal threshold) {
        // Validate input
        if (operator == null) {
            throw new IllegalArgumentException("Rule operator must not be null");
        }

        // Validate event value and threshold
        if (eventValue == null || threshold == null) {
            throw new IllegalArgumentException("Event value and threshold must not be null");
        }

        // Perform the comparison
        int comparisonResult = eventValue.compareTo(threshold);

        // Evaluate the comparison result based on the operator
        switch (operator) {
            case GREATER_THAN:
                return comparisonResult > 0;
            case LESS_THAN:
                return comparisonResult < 0;
            case GREATER_THAN_OR_EQUAL:
                return comparisonResult >= 0;
            case LESS_THAN_OR_EQUAL:
                return comparisonResult <= 0;
            case EQUALS:
                return comparisonResult == 0;
            default:
                // In case new operator is added and not handled
                throw new IllegalArgumentException("Invalid operator: " + operator);
        }
    }
}
