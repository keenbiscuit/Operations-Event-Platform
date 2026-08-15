-- V1__init_schema.sql
-- Flyway convention: V<version>__<description>.sql (double underscore after the version)

CREATE TABLE orgs (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL
);


CREATE TABLE org_memberships (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    org_id BIGINT NOT NULL REFERENCES orgs(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    user_role VARCHAR(50) NOT NULL
);

CREATE TABLE assets (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    org_id BIGINT NOT NULL REFERENCES orgs(id)
);

CREATE TABLE rules (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    condition_type VARCHAR(50) NOT NULL,
    threshold DECIMAL(10,2),
    org_id BIGINT NOT NULL REFERENCES orgs(id)
);

CREATE TABLE rule_assignments (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    asset_id BIGINT NOT NULL REFERENCES assets(id),
    rule_id BIGINT NOT NULL REFERENCES rules(id),
    severity VARCHAR(50) NOT NULL,
    threshold DECIMAL(10,2) NOT NULL,
    enabled BOOLEAN
    
);

CREATE TABLE events (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    asset_id BIGINT NOT NULL REFERENCES assets(id),
    event_type VARCHAR(50) NOT NULL,
    value DECIMAL(10,2) NOT NULL,
    occurred_at TIMESTAMP NOT NULL
);

CREATE TABLE alerts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    rule_assignment_id BIGINT NOT NULL REFERENCES rule_assignments(id),
    status VARCHAR(50) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    acknowledged_at TIMESTAMP,
    resolved_at TIMESTAMP,
    last_notified_at TIMESTAMP,
    notification_count INT DEFAULT 0
);

CREATE TABLE alert_events (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    alert_id BIGINT NOT NULL REFERENCES alerts(id),
    event_id BIGINT NOT NULL REFERENCES events(id),
    linked_at TIMESTAMP
);
