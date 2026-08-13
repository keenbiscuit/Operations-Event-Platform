package com.solomondev.op_event_platform.entity;

import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
@Table(name = "orgs")
@Getter
@Setter
public class Org {
    // Primary Key
    @Id
    // Added generated value to increment pk automatically
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Not null
    @Column(nullable = false)
    private String name;

    // So we can implement org.getMemberships
    // mapped by always referes to field name of other entity(owning side)
    @OneToMany(mappedBy = "org")
    private List<OrgMembership> orgMemberships = new ArrayList<>();

    // So we can implement org.GetAssets
    // mapped by always referes to field name of other entity(owning side)
    @OneToMany(mappedBy = "org")
    private List<Asset> assets = new ArrayList<>();

    public void addMembership(OrgMembership m) {
        orgMemberships.add(m);
        m.setOrg(this);
    }

    public void addAsset(Asset a) {
        assets.add(a);
        a.setOrg(this);
    }

}
