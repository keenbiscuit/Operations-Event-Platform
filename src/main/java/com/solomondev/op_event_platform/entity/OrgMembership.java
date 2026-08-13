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
@Table(name = "org_memberships", uniqueConstraints = {
        @jakarta.persistence.UniqueConstraint(columnNames = { "org_id", "user_id" }) })
@Getter
@Setter
public class OrgMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many OrgMemberships can belong to one org
    @ManyToOne
    @JoinColumn(name = "org_id", nullable = false)
    private Org org;

    //Many OrgMemberships can belong to one user
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String userRole;

}
