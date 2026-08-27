package com.solomondev.op_event_platform.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
    // Fk that connects Asset to org
    @ManyToOne
    @JoinColumn(name = "org_id", nullable = false)
    private Org org;

    // So we can implement assets.getRules
    // Show me every rule current applied to this asset
    @JsonIgnore
    @OneToMany(mappedBy = "asset")
    private List<RuleAssignment> ruleAssignments = new ArrayList<>();

    // Helper method
    // Add a RuleAssignment
    public void addRule(RuleAssignment ra) {
        ruleAssignments.add(ra);
        ra.setAsset(this);
    }
}
