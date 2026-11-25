# Operations Event Platform

[![Maven CI](https://github.com/keenbiscuit/Operations-Event-Platform/actions/workflows/maven-ci.yml/badge.svg?branch=main)](https://github.com/keenbiscuit/Operations-Event-Platform/actions/workflows/maven-ci.yml)

A Java and Spring Boot API for ingesting operational events, evaluating asset-specific rules, and creating auditable alerts. OIDC bearer tokens authenticate requests, and organization memberships determine which tenant data a user can access.

## Features

- Ingest events for assets the authenticated user’s organization can access
- Evaluate rule assignments and create alerts with event audit links
- Search and paginate events and alerts, including basic filters
- View events, alerts, and alert-event history
- Acknowledge and resolve alerts with lifecycle validation
- Provide tenant-scoped dashboard counts
- Validate PostgreSQL schema using Flyway migrations
- Document REST endpoints with OpenAPI / Swagger UI

## Architecture

```text
OIDC issuer -> JWT bearer token -> Spring Security resource server
                                      |
                                      v
                             JWT subject lookup
                                      |
                                      v
                         users.external_subject
                                      |
                                      v
                         org_memberships tenant scope
                                      |
                                      v
                 Spring MVC -> services -> JPA -> PostgreSQL
```

The API does not issue credentials or provision users. An administrator must map each OIDC issuer subject to a local user and assign organization memberships. Tenant checks are enforced in database queries and before event ingestion.

## Authentication and local setup

The API expects JWTs from a trusted OpenID Connect issuer. Set `OIDC_ISSUER_URI` to the issuer URL whose discovery metadata and signing keys should be trusted. Spring Security validates token signatures, issuer, and time claims. Send access tokens in the `Authorization: Bearer <token>` header.

For local development, set these environment variables:

```text
OIDC_ISSUER_URI=https://your-issuer.example.com/
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ops_platform
SPRING_DATASOURCE_USERNAME=ops_platform
SPRING_DATASOURCE_PASSWORD=your-local-password
```

The application requires a reachable PostgreSQL database. Flyway applies the migrations at startup, and Hibernate validates the resulting schema.

### Provisioning tenant access

1. Create a local `users` row for the person.
2. Set `users.external_subject` to the exact `sub` claim from the validated JWT.
3. Add an `org_memberships` row connecting that user to the organization they may access.

The external subject must be unique. Existing users with no subject cannot access tenant data until mapped. The application intentionally has no public user-registration or membership-management endpoint.

### Run

Prerequisites: Java 17, Maven, PostgreSQL, and Docker for Testcontainers integration tests.

```bash
mvn spring-boot:run
mvn test
```

The API starts at `http://localhost:8080`. The actuator health check is public at `GET /actuator/health`; application APIs require authentication.

## API

All API examples below require a valid bearer token and are scoped to the organizations mapped to its subject.

### Events

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/events` | Validate, save, and process an event for an accessible asset |
| `GET` | `/api/events` | Search events with pagination and filters |
| `GET` | `/api/events/{id}` | Retrieve an event and linked alert summaries |

Event request:

```json
{
  "assetId": 1,
  "eventType": "CPU_USAGE",
  "value": 95.0
}
```

Search parameters: `q`, `status`, `severity`, `from`, `to`, `page`, `size`, and `direction`. Status and severity filter events that have a linked alert with the requested value. Pages are zero-based, default to 20 items, and are capped at 100.

Example:

```text
GET /api/events?q=cpu&severity=HIGH&page=0&size=20&direction=DESC
```

A successful event creation returns `201 Created`, a `Location: /api/events/{id}` header, and the event representation. Invalid request data returns `400`; a missing or inaccessible asset returns `404`.

### Alerts

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/alerts` | Search and paginate alerts |
| `GET` | `/api/alerts/{id}` | Retrieve an alert and linked event history |
| `PATCH` | `/api/alerts/{id}/acknowledge` | Mark an alert acknowledged |
| `PATCH` | `/api/alerts/{id}/resolve` | Resolve an alert |

Search parameters: `status`, `severity`, `assetId`, `q`, `from`, `to`, `page`, `size`, and `direction`. Search text matches asset names and linked event types. Alert time filters use the alert creation time.

A resolved alert cannot be acknowledged or resolved again. Inaccessible alert IDs return `404`, which avoids confirming another tenant’s resources exist.

### Dashboard summary

`GET /api/dashboard/summary` returns tenant-scoped counts:

```json
{
  "totalEvents": 25,
  "activeAlerts": 3,
  "acknowledgedAlerts": 2,
  "resolvedAlerts": 11
}
```

### API documentation

When the application is running, OpenAPI is available at `/v3/api-docs` and Swagger UI at `/swagger-ui.html`. These endpoints are protected like other application endpoints.

## Testing

- Unit tests cover event ingestion, event processing, rule evaluation, and alert lifecycle behavior.
- MVC tests cover request validation, response contracts, pagination, and error mapping.
- PostgreSQL Testcontainers tests cover migrations, JPA mappings, tenant-scoped alert queries, and the event-to-alert persistence workflow.
- Security tests cover rejecting requests without authentication and accepting authenticated JWT requests.

Run the suite with `mvn test`. Docker must be running for Testcontainers tests.

## Deployment configuration

Configure `OIDC_ISSUER_URI` and the `SPRING_DATASOURCE_*` variables in the deployment environment. Do not commit issuer secrets, client secrets, signing keys, or production database credentials. The API is a resource server; the OIDC provider handles login and token issuance.

## Technology

Java 17 · Spring Boot 4.1 · Spring Security OAuth2 Resource Server · Spring Data JPA · PostgreSQL · Flyway · Maven · JUnit 5 · Mockito · MockMvc · Testcontainers · Docker
