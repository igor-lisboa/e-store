# Architecture and quality specification

## Current shape

- The Java web application has `application` servlet/API classes, `core` services/DTOs/interfaces, and `infrastructure` DAO/database helpers.
- `application/StoreServices` is the explicit composition root. Services receive repository interfaces through constructors; core code imports no HTTP, infrastructure, or JSON framework classes. Cart creation accepts an injectable clock.
- The project targets Java 8, Servlet 3.1, JSP, Maven WAR packaging, and a legacy webapp-runner. A runtime modernization remains a separate decision.
- Default Maven tests include isolated service/servlet tests and real H2 JDBC checks. `mysql-integration` verifies disposable MySQL migrations and checkout. `legacy-integration` runs reset-per-case database tests and English Cucumber scenarios.
- Playwright launches a real local servlet container and verifies browser security, four locales, Swagger rendering, and a database-backed customer shopping journey. Both enabled and disabled demo checkout are exercised.
- State-changing form actions require POST. A servlet filter checks a random session-bound CSRF token before browser controllers run; forms share a hidden-token fragment. Invalid/missing tokens receive HTTP 403. GET API endpoints and Swagger are outside the browser form filter.
- Catalog category-link mapping is separated from request orchestration. Java files are checked against the 300-line limit. Legacy database tests are organized by account, address, catalog, cart, checkout, and review behavior with reset-per-case fixtures, explicit failure assertions, and no random retry loops.
- Java source and test filenames and type names use English under `br.uff.store`. JSPs, stylesheets, feature filenames, OpenAPI documents, and the load-test filename use English too. Import, package-path, type-filename, and stylesheet reference checks guard these renames.
- JaCoCo produces measured line and branch reports during `verify`. There is no percentage threshold yet. Meaningful behavioral coverage is required; shallow assertions and tests added merely to raise a percentage are not accepted.
- DTOs still use legacy map-based row construction and floating-point money at some Java boundaries. These remain explicit refactoring tasks rather than evidence of a completed domain redesign.

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

- Local commands run the unit and integration suites; no hosted CI workflow is defined.
- E2E smoke journeys cover sign-in, catalog, cart, and checkout.
- Changes to a feature update its spec, tests, and translation keys.
- Track any remaining >300-line files in the backlog until split safely.

## Accessibility baseline

- Every visible form control has an accessible name: a `label for` that points at a real id, or an `aria-label`/`aria-labelledby`. A repository contract test scans every page and fails on an unlabeled control or a label that points at a missing id. The rating stars form a labelled radio group, the price sliders are named by their visible labels, and the cart quantity boxes include the product name.
- Registration and new-password fields enforce the 8-character minimum in the browser as well as on the server.
- Keyboard order, focus visibility, and colour contrast have not been audited with a tool; they remain open in the backlog.
