# Backlog and pending work

Update this active list when work is evidenced complete or new gaps are found. Specifications describe target behavior; this backlog tracks incomplete implementation.

## P0 — Security and correctness

- [ ] Finish the plaintext-password migration: PBKDF2 hashing and login-time upgrades are implemented, but existing records remain plaintext until their owners sign in. Define and execute a safe migration for dormant accounts.
- [ ] Finish auditing authorization and ownership for every admin, rating, favorite, and cart action. Account edits, address mutation, product administration, review input, session-bound carts, and the user API now enforce server-side checks.
- [ ] Add CSRF tokens to all state-changing forms and validate them server-side; POST-only checks do not prevent cross-site POST requests.
- [x] Remove stack traces and exception serialization from API responses; use a shared JSON error object and client/server status codes.
- [ ] Review SQL transaction handling, JDBC resource closure, rollback behavior, and DAO cleanup.
- [ ] Finish validation for product prices, paging, account/address fields, and every other external value. Cart quantities/stock and review input are now checked in services.
- [ ] Replace binary floating-point money calculations with decimal-safe values.
- [ ] Remove credentials and personal data from seed data; create safe deterministic fixtures.

## P1 — Test coverage

- [ ] Add isolated unit tests for catalog, account, cart, checkout, rating, favorite, and authorization rules.
- [ ] Replace shared mutable database setup with disposable, repeatable test fixtures.
- [ ] Add repository and transaction integration tests against a disposable MySQL service.
- [ ] Add servlet/API integration tests with request, response, and session assertions.
- [ ] Add browser E2E coverage for sign-in, catalog search, cart handoff, checkout, favorites, and ratings.
- [ ] Make Cucumber steps and features English and assert user-visible outcomes.
- [ ] Translate legacy Portuguese identifiers, comments, logs, and technical strings to English as affected areas are refactored.
- [x] Rename maintained source/test files, Java types/packages, JSPs, CSS assets, feature files, and root test collections to English and update references.
- [ ] Add CI for static checks, unit, integration, and E2E suites.
- [x] Add initial CI for repository contracts, isolated Java tests, and migration apply/validate/reapply. Browser E2E and isolated repository integration coverage remain pending.

## P1 — Localization and accessibility

- [ ] Localize every JSP label, title, button, validation message, and server message in all four locales.
- [ ] Format currency and dates by locale.
- [ ] Test locale choice, persistence, fallback, and keyboard access through integration and browser tests.
- [ ] Audit semantic labels, keyboard navigation, contrast, and accessible errors.

## P1 — Local developer setup

- [ ] Verify Compose startup, schema initialization, Maven tests, and shutdown on Windows, macOS, and Linux.
- [x] Separate schema/routine/reference migrations from optional local fixtures and run them with Flyway before startup.
- [ ] Execute Flyway against MySQL in CI and verify adoption of an existing database with preserved data.
- [ ] Add a safe, documented database reset and fixture strategy.
- [ ] Pin supported Java, Maven, MySQL, and Compose versions; review the MySQL runtime lifecycle.

## P2 — Architecture and maintainability

- [ ] Inject repository interfaces; remove service constructors that instantiate concrete DAOs.
- [ ] Separate domain rules, application use cases, HTTP adapters, and JDBC implementations with inward dependency direction.
- [x] Extract catalog category selection so production Java files stay below 300 lines.
- [ ] Replace the split legacy integration-test batches with feature-focused classes and isolated fixtures. File-size splitting is complete; fixture isolation remains.
- [ ] Replace map-backed persistence mapping with explicit typed row mapping.
- [ ] Add input validation objects and consistent domain errors.
- [ ] Replace stored-procedure-only checkout orchestration with a tested transaction boundary.
- [ ] Document API routes, auth, status codes, payloads, and error schema.
- [x] Replace the Postman collection with packaged Swagger UI and an OpenAPI contract for current read endpoints, session authentication, response schemas, and errors.
- [ ] Exercise Swagger UI and OpenAPI responses against a running servlet container; add dedicated 404 responses.
- [ ] Decide whether to migrate Java EE 7/Servlets/JSP or maintain the current stack.

## P2 — Product decisions

- [ ] Confirm page size, out-of-stock visibility, cart expiry/merge policy, review-edit policy, password policy, and payment scope.
- [ ] Decide whether anonymous carts must survive a new browser session; if so, design a signed opaque cart token and migration from the legacy numeric cookie.
- [ ] Define production deployment, observability, backups, secret management, and data retention requirements.
