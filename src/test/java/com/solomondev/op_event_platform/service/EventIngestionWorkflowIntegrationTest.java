package com.solomondev.op_event_platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.solomondev.op_event_platform.entity.Alert;
import com.solomondev.op_event_platform.entity.AlertEvent;
import com.solomondev.op_event_platform.entity.Asset;
import com.solomondev.op_event_platform.entity.Event;
import com.solomondev.op_event_platform.entity.Org;
import com.solomondev.op_event_platform.entity.Rule;
import com.solomondev.op_event_platform.entity.RuleAssignment;
import com.solomondev.op_event_platform.entity.enums.RuleOperator;
import com.solomondev.op_event_platform.model.dto.CreateEventRequestDto;
import com.solomondev.op_event_platform.repository.AlertEventRepository;
import com.solomondev.op_event_platform.repository.AlertRepository;
import com.solomondev.op_event_platform.repository.AssetRepository;
import com.solomondev.op_event_platform.repository.OrgRepository;
import com.solomondev.op_event_platform.repository.RuleAssignmentRepository;
import com.solomondev.op_event_platform.repository.RuleRepository;

@DataJpaTest
@Testcontainers
@Import({
        EventIngestionWorkflowIntegrationTest.ContainerConfiguration.class,
        EventIngestionService.class,
        EventProcessingService.class,
        RuleEvaluationService.class,
        RuleEvaluator.class
})
class EventIngestionWorkflowIntegrationTest {

    @Autowired
    private EventIngestionService eventIngestionService;

    @Autowired
    private OrgRepository orgRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private RuleAssignmentRepository ruleAssignmentRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private AlertEventRepository alertEventRepository;

    private Org org;
    private Asset asset;
    private Rule rule;
    private RuleAssignment ruleAssignment;

    @TestConfiguration(proxyBeanMethods = false)
    static class ContainerConfiguration {

        @Bean
        @ServiceConnection
        PostgreSQLContainer<?> postgresContainer() {
            return new PostgreSQLContainer<>("postgres:16-alpine");
        }
    }

    @BeforeEach
    void setUp() {
        org = new Org();
        org.setName("Test Org");
        org = orgRepository.save(org);

        asset = new Asset();
        asset.setName("Payment Service");
        asset.setType("SERVICE");
        asset.setOrg(org);
        asset = assetRepository.save(asset);

        rule = new Rule();
        rule.setConditionType("PAYMENT");
        rule.setThreshold(new BigDecimal("100.00"));
        rule.setOperator(RuleOperator.GREATER_THAN);
        rule.setOrg(org);
        rule = ruleRepository.save(rule);

        ruleAssignment = new RuleAssignment();
        ruleAssignment.setAsset(asset);
        ruleAssignment.setRule(rule);
        ruleAssignment.setSeverity("HIGH");
        ruleAssignment.setThreshold(new BigDecimal("100.00"));
        ruleAssignment.setEnabled(true);
        ruleAssignment = ruleAssignmentRepository.save(ruleAssignment);
    }

    @Test
    void createsAlertAndLinksItToEventWhenRuleMatches() {
        Event savedEvent = eventIngestionService.ingestEvent(
                new CreateEventRequestDto(
                        asset.getId(),
                        "PAYMENT",
                        new BigDecimal("150.00")));

        List<Alert> alerts = alertRepository.findByRuleAssignment_Asset_IdAndStatus(
                asset.getId(),
                "OPEN");

        assertEquals(1, alerts.size());

        Alert savedAlert = alerts.get(0);

        assertEquals(ruleAssignment.getId(), savedAlert.getRuleAssignment().getId());
        assertEquals("OPEN", savedAlert.getStatus());
        assertEquals("HIGH", savedAlert.getSeverity());
        assertEquals(0, savedAlert.getNotificationCount());

        List<AlertEvent> alertEvents = alertEventRepository.findAll();

        assertEquals(1, alertEvents.size());
        assertEquals(savedAlert.getId(), alertEvents.get(0).getAlert().getId());
        assertEquals(savedEvent.getId(), alertEvents.get(0).getEvent().getId());
    }
}