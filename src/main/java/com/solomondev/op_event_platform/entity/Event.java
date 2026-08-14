package com.solomondev.op_event_platform.entity;

import java.math.BigDecimal;
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
@Table(name = "events")
@Getter
@Setter
public class Event {
    

    // Primary Key
    @Id
    // Added generated value to increment pk automatically
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //One Asset can have many events
    @ManyToOne
    @JoinColumn(name = "asset", nullable = false)
    private Asset assetId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private BigDecimal value;

    @Column(nullable = false)
    private LocalDateTime occurredAt;
    
}
