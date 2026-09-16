package com.solomondev.op_event_platform.repository;

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
import com.solomondev.op_event_platform.entity.Asset;
import com.solomondev.op_event_platform.entity.Org;
import com.solomondev.op_event_platform.entity.Rule;
import com.solomondev.op_event_platform.entity.RuleAssignment;
import com.solomondev.op_event_platform.entity.enums.RuleOperator;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
@Testcontainers
@Import(AlertRepositoryIntegrationTest.ContainerConfiguration.class)
class AlertRepositoryIntegrationTest {
    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private OrgRepository orgRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private RuleAssignmentRepository ruleAssignmentRepository;

    @TestConfiguration(proxyBeanMethods = false)
    static class ContainerConfiguration {

        @Bean
        @ServiceConnection
        PostgreSQLContainer<?> postgresContainer() {
            return new PostgreSQLContainer<>("postgres:16-alpine");
        }
    }

    @Test
    void findsAlertsByAssetIdAndStatus() {
        Org org = new Org();
        org.setName("Test Org");
        org = orgRepository.save(org);

        Asset asset = new Asset();
        asset.setName("Payment Service");
        asset.setType("SERVICE");
        asset.setOrg(org);
        asset = assetRepository.save(asset);

        Rule rule = new Rule();
        rule.setConditionType("CPU_USAGE");
        rule.setThreshold(new BigDecimal("80.00"));
        rule.setOperator(RuleOperator.GREATER_THAN);
        rule.setOrg(org);
        rule = ruleRepository.save(rule);

        RuleAssignment ruleAssignment = new RuleAssignment();
        ruleAssignment.setAsset(asset);
        ruleAssignment.setRule(rule);
        ruleAssignment.setSeverity("HIGH");
        ruleAssignment.setThreshold(new BigDecimal("80.00"));
        ruleAssignment.setEnabled(true);
        ruleAssignment = ruleAssignmentRepository.save(ruleAssignment);

        Alert openAlert = new Alert();
        openAlert.setRuleAssignment(ruleAssignment);
        openAlert.setStatus("OPEN");
        openAlert.setSeverity("HIGH");
        openAlert.setCreatedAt(LocalDateTime.now());
        openAlert = alertRepository.save(openAlert);

        Alert resolvedAlert = new Alert();
        resolvedAlert.setRuleAssignment(ruleAssignment);
        resolvedAlert.setStatus("RESOLVED");
        resolvedAlert.setSeverity("HIGH");
        resolvedAlert.setCreatedAt(LocalDateTime.now());
        alertRepository.save(resolvedAlert);

        List<Alert> results = alertRepository.findByRuleAssignment_Asset_IdAndStatus(
                asset.getId(),
                "OPEN");

        assertEquals(1, results.size());
        assertEquals(openAlert.getId(), results.get(0).getId());
        assertEquals("OPEN", results.get(0).getStatus());
    }
}