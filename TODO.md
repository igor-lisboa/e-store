# Backlog and pending work

Use [WORK_LOG.md](WORK_LOG.md) for the latest verified checkpoint and exact resume commands. Check items only when their behavior has been demonstrated. Keep unavailable tooling and product decisions at the end; continue unblocked work first.

## Next implementation tasks

- [ ] Expand meaningful coverage of remaining authorization branches and malformed controller input. Default-suite coverage is still low; review JaCoCo branches before choosing tests, and reject shallow coverage inflation. Progress: `AddressServlet` and `ProductAdminServlet` have injectable construction and default tests for guest, customer, and admin denial, method rejection, session-owned writes, and malformed IDs. Remaining controllers with zero default branch coverage: `UserServlet`, `CheckoutServlet`, `ReviewServlet`, `ProductServlet`, `ProductsServlet` (paging helper only).
- [ ] Complete the authorization audit across every account/address/admin/review/favorite/cart action. Current checks cover account edits, address ownership, product administration, reviews, favorites, session carts, and user API access. Add hostile owner/admin/guest browser cases for remaining flows. `GET /api/products/` is administrator-only (401 signed out, 403 customers). Browser hostile cases remain unrun.
- [ ] Single-product API stock exposure (decision 1B): anonymous and customer callers receive availability only; administrators keep exact quantity. Update `ProductDTO` serialization, OpenAPI, and the browser stock checks that read `quantity` anonymously.
- [ ] Out-of-stock visibility (decision 2A): keep out-of-stock products listed and marked unavailable. Change the storefront listing query (currently `quantity > 0`), the product page, and the featured list, with tests and locale strings.
- [ ] Anonymous cart policy (decision 5C): merge the anonymous cart into the account cart on sign-in, and expire anonymous carts after inactivity. The expiry period is not chosen yet; 30 days is a proposed default to confirm. Use a signed opaque token only if recovery across sessions is added.
- [ ] Provider-agnostic checkout (decision 6): define payment and delivery ports in core with provider-neutral data (provider name, external reference, status, amounts, delivery method, tracking code, address snapshot, and raw-response reference). Keep the local demo as the only adapter until a vendor is chosen. Vendor choice remains open.
- [ ] Customer cancellation (decision 7B): customers may cancel an order before fulfillment, which restores stock. Requires order states and a versioned migration.
- [ ] Order line snapshots (decision 8A): store product name, unit price, and quantity on each sale line at checkout through a versioned migration and an updated `checkout_cart` procedure.
- [ ] Customer review editing and deletion (decision 10C): customers edit and delete only their own reviews, with ownership checks, OpenAPI/browser coverage where routes change.
- [ ] Dormant-account password reset (decision 4A): forced reset by email on next sign-in attempt. Blocked until an email delivery provider is chosen; the repository has no mail infrastructure.
- [ ] Review the existing catalog and account pages for stale wording after the paging and password changes, and update translations in all four bundles.
- [ ] Localize remaining page titles, placeholders, JavaScript validation/confirmation messages, and dynamic server feedback in all four bundles.
- [ ] Audit form label associations, keyboard navigation, focus handling, contrast, and accessible validation errors.
- [ ] Replace map-backed DTO persistence construction with explicit typed row mapping outside core.
- [ ] Replace remaining abbreviated/mixed-language legacy implementation identifiers and technical comments as found. External ViaCEP response keys and immutable historical migrations retain their original contracts.
- [ ] Review all remaining input boundaries, including maximum lengths, malformed query values, stock-increment overflow, image payloads, and user-provided URLs.
- [ ] Verify and serialize cart-item mutations racing with checkout, and define immutable purchased-item/price snapshots. Current contention tests cover competing checkouts; they do not prove an in-flight cart edit cannot race the sold-state check. Overlaps with the order line snapshot task.
- [ ] Add controlled infrastructure-failure checks for real-container API 500 behavior. Malformed IDs, missing records, protected account ownership, and guest/customer/admin access now have real-container evidence; mocked servlet tests cover infrastructure error mapping.
- [ ] Replace the Java 8 / servlet runtime only after the production target is defined (decision 14B). Keep a separately tested compatibility change when that happens.

## Completed and evidenced foundations

- [x] Create specialty specifications, root README, backlog, and shared Codex/Claude/Copilot guidance.
- [x] Add shared feature/documentation, Swagger maintenance, and meaningful-test/build review skills and agent checklists.
- [x] Require English names, SOLID, simple junior-readable code, Clean Architecture, files below 300 lines, and meaningful tests in all assistant guidelines.
- [x] Require assistants to number open decisions, list lettered choices, and give pros and cons as bullets (Codex, Claude, and Copilot guidance).
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
- [x] Monetary values use `BigDecimal` end to end in product, cart, sale, and filter DTOs. Prices round half-up to two decimal places before persistence, and the rounded value must fit `DECIMAL(11,2)` (decision 13B). Default suite: 107 tests pass with unit tests for rounding and bounds; the legacy MySQL money assertions have compiled but not run.
- [x] Storefront paging shows 20 items by default and accepts an optional `pageSize` query value from 1 to 100; other values use the default (decision 9C). Unit-tested; the browser suite has not run.
- [x] Passwords set on account creation or replacement must have at least 8 characters, with no other composition rules (decision 11A). Unit-tested; existing hashes are not re-checked.
- [x] Production data starts fresh from versioned migrations; legacy or unknown databases are not adopted (decision 3A).

## Blocked or deferred decisions (work around these)

- [ ] Execute full Compose startup, migration ordering, browser tests, and shutdown. Docker Desktop is now available on this workstation: the `database` service became healthy, `migrate migrate`, `migrate validate`, and re-apply succeeded, and the MySQL and legacy Maven suites passed against the Compose database. Still unverified: the `app` service, browser suites against Compose, and Linux and macOS startup. The CI health check failure seen on the hosted runner is not yet explained; the health check was hardened, and the hosted logs must confirm it.
- [ ] Run the hosted CI workflow and address platform-specific issues; local checks cannot establish the hosted result.
- [ ] Choose the payment and delivery vendors that plug into the provider-agnostic checkout ports (decision 6). The ports themselves are a backlog task above.
- [ ] Choose an email delivery provider for the dormant-account reset (decision 4A).
- [ ] Decide production deployment target (decision 12C: keep local-only for now). Still required: supported runtime lifecycle, observability, backups, secret management, and retention.
