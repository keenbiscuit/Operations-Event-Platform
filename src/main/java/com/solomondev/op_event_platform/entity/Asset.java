package com.solomondev.op_event_platform.entity;

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
@Table(name = "assets")
@Getter
@Setter
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Not null
    @Column(nullable = false)
    private String name;

    // Not null
    @Column(nullable = false)
    private String type;

    // Many assets can belong to one Org
    // Fk that connects asset to org
    @ManyToOne
    @JoinColumn(name = "org_id", nullable = false)
    private Org org;

}
