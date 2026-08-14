package com.solomondev.op_event_platform.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "alerts")
@Getter
@Setter
public class Alert {

    // Primary Key
    @Id
    // Added generated value to increment pk automatically
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "rule_assignment_id", nullable = false)
    private RuleAssignment ruleAssignment;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String severity;

    private Integer notificationCount = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime acknowledgedAt;

    private LocalDateTime resolvedAt;

    private LocalDateTime lastNotifiedAt;

}
