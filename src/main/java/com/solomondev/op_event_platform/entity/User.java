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

    //Primary key
    @Id
    //Added generated value to increment pk automatically
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Not null
    @Column(nullable = false)
    private String username;

    //Not null
    @Column(nullable = false)
    private String passwordHash;

    // One-to-many relationship for bi-directional mapping
    @OneToMany(mappedBy = "user")
    private List<OrgMembership> orgMemberships = new ArrayList<>();

    // Helper method to add a membership
    public void addMembership(OrgMembership m) {
        orgMemberships.add(m);
        m.setUser(this);
    }
}
