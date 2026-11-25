package com.solomondev.op_event_platform.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.solomondev.op_event_platform.entity.Alert;
import com.solomondev.op_event_platform.entity.Asset;
import com.solomondev.op_event_platform.entity.Org;
import com.solomondev.op_event_platform.entity.OrgMembership;
import com.solomondev.op_event_platform.entity.Rule;
import com.solomondev.op_event_platform.entity.RuleAssignment;
import com.solomondev.op_event_platform.entity.User;
import com.solomondev.op_event_platform.entity.enums.RuleOperator;

@DataJpaTest
@Testcontainers
@Import(AlertRepositoryIntegrationTest.ContainerConfiguration.class)
class AlertRepositoryIntegrationTest {
    @Autowired private AlertRepository alertRepository;
    @Autowired private OrgRepository orgRepository;
    @Autowired private AssetRepository assetRepository;
    @Autowired private RuleRepository ruleRepository;
    @Autowired private RuleAssignmentRepository ruleAssignmentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private OrgMembershipRepository orgMembershipRepository;

    private Org org;
    private Rule rule;
    private User user;

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
        org.setName("Tenant One");
        org = orgRepository.save(org);

        user = new User();
        user.setUsername("tenant-user");
        user.setPasswordHash("unused-for-oidc");
        user.setExternalSubject("oidc-subject");
        user = userRepository.save(user);

        OrgMembership membership = new OrgMembership();
        membership.setOrg(org);
        membership.setUser(user);
        membership.setUserRole("MEMBER");
        orgMembershipRepository.save(membership);

        rule = new Rule();
        rule.setConditionType("CPU_USAGE");
        rule.setThreshold(new BigDecimal("80.00"));
        rule.setOperator(RuleOperator.GREATER_THAN);
        rule.setOrg(org);
        rule = ruleRepository.save(rule);
    }

    @Test
    void searchesAlertsOnlyForOrganizationsTheSubjectBelongsTo() {
        Alert tenantAlert = createAlert(org, rule, "OPEN");

        Org otherOrg = new Org();
        otherOrg.setName("Tenant Two");
        otherOrg = orgRepository.save(otherOrg);
        Rule otherRule = new Rule();
        otherRule.setConditionType("CPU_USAGE");
        otherRule.setThreshold(new BigDecimal("80.00"));
        otherRule.setOperator(RuleOperator.GREATER_THAN);
        otherRule.setOrg(otherOrg);
        otherRule = ruleRepository.save(otherRule);
        createAlert(otherOrg, otherRule, "OPEN");

        Page<Alert> visible = alertRepository.searchAccessible(
                "oidc-subject", null, null, null, "", java.time.LocalDateTime.of(1, 1, 1, 0, 0), java.time.LocalDateTime.of(9999, 12, 31, 23, 59, 59, 999_999_000), PageRequest.of(0, 20));
        Page<Alert> hiddenForUnknownSubject = alertRepository.searchAccessible(
                "not-a-member", null, null, null, "", java.time.LocalDateTime.of(1, 1, 1, 0, 0), java.time.LocalDateTime.of(9999, 12, 31, 23, 59, 59, 999_999_000), PageRequest.of(0, 20));

        assertEquals(1, visible.getTotalElements());
        assertEquals(tenantAlert.getId(), visible.getContent().get(0).getId());
        assertTrue(hiddenForUnknownSubject.isEmpty());
    }

    @Test
    void appliesStatusFilterWithinAccessibleTenant() {
        Alert openAlert = createAlert(org, rule, "OPEN");
        createAlert(org, rule, "RESOLVED");

        Page<Alert> results = alertRepository.searchAccessible(
                "oidc-subject", "OPEN", null, null, "", java.time.LocalDateTime.of(1, 1, 1, 0, 0), java.time.LocalDateTime.of(9999, 12, 31, 23, 59, 59, 999_999_000), PageRequest.of(0, 20));

        assertEquals(1, results.getTotalElements());
        assertEquals(openAlert.getId(), results.getContent().get(0).getId());
    }

    private Alert createAlert(Org alertOrg, Rule alertRule, String status) {
        Asset asset = new Asset();
        asset.setName("Asset " + status + " " + System.nanoTime());
        asset.setType("SERVICE");
        asset.setOrg(alertOrg);
        asset = assetRepository.save(asset);

        RuleAssignment assignment = new RuleAssignment();
        assignment.setAsset(asset);
        assignment.setRule(alertRule);
        assignment.setSeverity("HIGH");
        assignment.setThreshold(new BigDecimal("80.00"));
        assignment.setEnabled(true);
        assignment = ruleAssignmentRepository.save(assignment);

        Alert alert = new Alert();
        alert.setRuleAssignment(assignment);
        alert.setStatus(status);
        alert.setSeverity("HIGH");
        alert.setCreatedAt(LocalDateTime.now());
        return alertRepository.save(alert);
    }
}
