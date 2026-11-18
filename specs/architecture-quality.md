# Architecture and quality specification

## Current shape

- The Java web application has `application` servlet/API classes, `core` services/DTOs/interfaces, and `infrastructure` DAO/database helpers.
- Services construct concrete DAOs in several no-argument constructors, limiting isolation and substitutability.
- The project targets Java 8, Java EE 7 APIs, JSP, Servlets, Maven WAR packaging, and a legacy webapp-runner.
- The repository has service tests that require MySQL and Cucumber feature tests. It has no isolated browser E2E suite.

## Target requirements

- Keep domain rules independent of servlet, JSP, JDBC, and vendor APIs.
- Put use-case orchestration in application services and depend on repository interfaces at inner boundaries.
- Keep HTTP controllers thin: parse/validate requests, invoke a use case, and map results.
- Use constructor injection; avoid service-locator and global mutable state patterns.
- Keep classes and files below 300 lines where practical. Split by responsibility and preserve cohesive types.
- Prefer explicit types, immutable values where useful, small methods, clear names, and meaningful domain exceptions.
- Keep all source code, comments, API docs, and technical documentation in English. User-facing translations remain in locale bundles.

## Required test layers

- Unit: domain rules and application use cases, isolated from MySQL and the servlet container.
- Integration: repositories, transactions, schema/migrations, and HTTP endpoints against disposable services.
- End-to-end: key user journeys in a real browser against the locally composed application.
- Tests must be deterministic, isolated, repeatable, and assert outcomes. Mutation testing is optional and complements these layers.

## Acceptance checks

- CI and local commands run the same unit and integration suites.
- E2E smoke journeys cover sign-in, catalog, cart, and checkout.
- Changes to a feature update its spec, tests, and translation keys.
- Track any remaining >300-line files in the backlog until split safely.
