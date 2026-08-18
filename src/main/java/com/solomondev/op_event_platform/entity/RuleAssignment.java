package com.solomondev.op_event_platform.entity;

import java.math.BigDecimal;
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
@Table(name = "rule_assignments")
@Getter
@Setter
public class RuleAssignment {

    // Primary key
    @Id
    // Added generated value to increment pk automatically
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key
    @ManyToOne
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    // Foreign key
    @ManyToOne
    @JoinColumn(name = "rule_id", nullable = false)
    private Rule rule;

    // Severity of the consequence if rule is broken
    @Column(nullable = false)
    private String severity;

    @Column(nullable = false)
    private BigDecimal threshold;

    @Column(nullable = false)
    private Boolean enabled;

}
