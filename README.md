# Operations Event Platform

A Java and Spring Boot backend for ingesting operational events, evaluating asset-specific rules, and creating alerts when an event matches a configured rule.

The project models an operations-monitoring workflow: an asset sends an event, the platform persists it, evaluates applicable rules, creates auditable alerts for matches, and exposes REST endpoints to retrieve and manage the alert lifecycle.

## Features

- Ingest operational events through a REST API
- Persist organizations, assets, events, rules, rule assignments, alerts, and alert-event audit records
- Evaluate rule conditions against incoming event values
- Create alerts automatically when assigned rules match
- Retrieve all alerts, filter by status, filter by asset ID, or filter by both asset ID and status
- Acknowledge and resolve alerts through lifecycle endpoints
- Reject invalid lifecycle actions, including acknowledging a resolved alert or resolving an alert twice
- Return consistent `404 Not Found` and validation/lifecycle `400 Bad Request` API errors
- Manage the PostgreSQL schema with Flyway migrations
- Cover service and MVC behavior with JUnit 5, Mockito, and MockMvc
