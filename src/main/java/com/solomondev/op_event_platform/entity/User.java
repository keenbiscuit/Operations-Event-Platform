package com.solomondev.op_event_platform.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(name = "external_subject", unique = true)
    private String externalSubject;

    @OneToMany(mappedBy = "user")
    private List<OrgMembership> orgMemberships = new ArrayList<>();

    public void addMembership(OrgMembership membership) {
        orgMemberships.add(membership);
        membership.setUser(this);
    }
}
