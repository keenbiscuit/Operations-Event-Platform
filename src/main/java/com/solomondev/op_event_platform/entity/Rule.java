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
@Table(name = "rules")
@Getter
@Setter
public class Rule {
    
    // Primary key
    @Id
    // Added generated value to increment pk automatically
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false)
    private String conditionType;

    // "Ex: create a payment alert when payment above: threshold"
    private BigDecimal threshold;

    // Many Rules can belong to one Org
    // Fk that connects Rule to org
    @ManyToOne
    @JoinColumn(name = "org_id", nullable = false)
    private Org org;


    
}
