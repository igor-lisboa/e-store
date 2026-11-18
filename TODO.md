# Backlog and pending work

Use [WORK_LOG.md](WORK_LOG.md) for the latest verified checkpoint and exact resume commands. Check items only when their behavior has been demonstrated. Keep unavailable tooling and product decisions at the end; continue unblocked work first.

## Next implementation tasks

- [ ] Expand meaningful coverage of remaining authorization branches and malformed controller input. Default-suite coverage is still low; review JaCoCo branches before choosing tests, and reject shallow coverage inflation.
- [ ] Complete the authorization audit across every account/address/admin/review/favorite/cart action. Current checks cover account edits, address ownership, product administration, reviews, favorites, session carts, and user API access. Add hostile owner/admin/guest browser cases for remaining flows.
- [ ] Replace Java floating-point monetary DTOs and calculations with decimal values end to end. Database checkout already calculates DECIMAL totals and has a 0.10 x 3 regression test.
- [ ] Localize remaining page titles, placeholders, JavaScript validation/confirmation messages, and dynamic server feedback in all four bundles.
- [ ] Audit form label associations, keyboard navigation, focus handling, contrast, and accessible validation errors.
- [ ] Replace map-backed DTO persistence construction with explicit typed row mapping outside core.
- [ ] Replace remaining abbreviated/mixed-language legacy implementation identifiers and technical comments as found. External ViaCEP response keys and immutable historical migrations retain their original contracts.
- [ ] Review all remaining input boundaries, including maximum lengths, malformed query values, stock-increment overflow, image payloads, and user-provided URLs.
- [ ] Verify and serialize cart-item mutations racing with checkout, and define immutable purchased-item/price snapshots. Current contention tests cover competing checkouts; they do not prove an in-flight cart edit cannot race the sold-state check.
- [ ] Add controlled infrastructure-failure checks for real-container API 500 behavior. Malformed IDs, missing records, protected account ownership, and guest/customer/admin access now have real-container evidence; mocked servlet tests cover infrastructure error mapping.
- [ ] Modernize the legacy servlet runner and runtime dependencies in a separately tested compatibility change. Preserve Java 8 behavior until that change is planned.

## Completed and evidenced foundations

- [x] Create specialty specifications, root README, backlog, and shared Codex/Claude/Copilot guidance.
- [x] Add shared feature/documentation, Swagger maintenance, and meaningful-test/build review skills and agent checklists.
- [x] Require English names, SOLID, simple junior-readable code, Clean Architecture, files below 300 lines, and meaningful tests in all assistant guidelines.
- [x] Rename maintained source/test files, packages/types, routes, API properties, Java methods/accessors, and major form/style hooks to English, updating callers together.
- [x] Replace Postman with bundled Swagger UI and OpenAPI, including missing-resource 404 responses and protected account endpoints.
- [x] Remove exception/stack-trace serialization from JSON errors and hide credential fields.
- [x] Implement PBKDF2 passwords, login-time legacy upgrades, session rotation, and remembered email without password cookies.
- [x] Add POST-only browser mutations and session-bound CSRF validation, including missing/foreign/expired token denial.
- [x] Restrict sign-in redirects to known local destinations and HTML-escape untrusted rendered model/flash content.
- [x] Refactor JDBC to per-operation connections, deterministic cleanup, private transaction boundaries, rollback, and preserved original/suppressed errors.
- [x] Test JDBC commit/rollback, binding, null mapping, cleanup failures, and shared-DAO concurrency with H2 and failure-injection checks.
- [x] Validate product prices/stock, pagination bounds, account/address inputs, review bounds, and relevant IDs at service boundaries.
- [x] Inject repository interfaces through an explicit application composition root; prevent core imports of infrastructure, HTTP, and JSON frameworks.
- [x] Make cart creation time injectable and remove infrastructure helpers/JSON serialization from core DTOs.
- [x] Replace startup SQL dumps with versioned schema, view, routine, reference-data, and optional synthetic development-seed migrations.
- [x] Verify MySQL migration clean application, validation, reapply, checkout rollback, decimal totals, duplicate order rejection, address ownership, and competing-cart stock allocation.
- [x] Replace the stock-leaking checkout procedure with atomic `checkout_cart` in V6; rename the review table with V7 without rewriting applied history.
- [x] Reset disposable database fixtures before each legacy integration/Cucumber case and convert Cucumber scenarios to English with persisted outcome assertions.
- [x] Replace numbered legacy batches with account, address, catalog, cart, checkout, and review test classes. Remove random retry loops, swallowed exceptions, vacuous price/history checks, and broad existence-only assertions; retain 33 meaningful database cases.
- [x] Require explicit demo checkout mode; remove client-only payment verification and misleading payment/shipping claims.
- [x] Add and pass final-state browser smoke tests and customer/admin journeys in both demo modes, including stock effects, disabled-demo completion denial, API error/ownership checks, and hidden credentials. Pass default, MySQL, and legacy/Cucumber suites after English renames and test refactoring.
- [x] Expand common page labels across four locales and format BRL currency/order/review dates using the selected locale.
- [x] Verify locale bundle key completeness, formatting, allowlist behavior, and representative browser switching across all four locales.
- [x] Add JaCoCo line/branch reports without claiming full coverage or introducing an arbitrary threshold.
- [x] Define CI for static/OpenAPI checks, MySQL migrations, default/MySQL/legacy tests, browser demo-enabled/disabled journeys, and report uploads. Hosted execution remains unverified.
- [x] Keep maintained Java/JSP/CSS/Python/specification files below 300 lines; split account styles by responsibility.
- [x] Provide one-command `sh scripts/start.sh`, pinned MySQL/Flyway, persistent volumes, documented local reset, and synthetic local accounts.

## Blocked or deferred decisions (work around these)

- [ ] Execute full Compose startup, migration ordering, browser tests, and shutdown on a Docker-enabled machine. Docker is unavailable on this workstation; native Java/MySQL/Edge checks do not prove Compose execution. Verify Windows, macOS, and Linux startup before claiming cross-platform support.
- [ ] Run the hosted CI workflow and address platform-specific issues; local checks cannot establish the hosted result.
- [ ] Adopt any existing valuable database only after backup and schema/routine/seed comparison. Do not baseline unknown data or attach old MySQL 5.7 volumes directly to MySQL 8.
- [ ] Choose and safely execute a dormant-account password migration/reset strategy. Login-time hashing does not upgrade users who never return.
- [ ] Decide anonymous-cart expiry, merge, and cross-session recovery policy. A signed opaque token is required if recovery is implemented; raw numeric cart cookies are not trusted.
- [ ] Confirm payment provider, order-price audit history, cancellation/refund policy, fulfillment, and shipping requirements. Current checkout is a local demonstration without charges or delivery.
- [ ] Confirm page-size policy, out-of-stock visibility, review-edit policy, and password requirements beyond implemented technical limits.
- [ ] Define production deployment, supported runtime lifecycle, observability, backups, secret management, and retention requirements.
