# Autonomous maintenance checkpoint

Updated 2026-11-18. The user authorized autonomous pending work and asked for completed/missing tracking and a commit title. Changes are uncommitted; preserve the entire working tree. No subagents were used.

## Current checkpoint

Implementation and final verification are complete for this maintenance batch. Continue the unblocked tasks in `TODO.md`; decisions and unavailable tooling are listed at its end. The entire requested backlog is not complete. No commit was created.

## Completed during this run

- Downloaded portable Java 8, Maven 3.9.9, and native MySQL 8.4.13 into ignored `.local-tools/`. No Windows services were installed.
- Fixed the real build failure caused by missing servlet resource bundles in the broad Java EE API dependency; use the specific Servlet 3.1 provided API.
- Replaced shared mutable JDBC connections and manual cleanup with per-operation resource ownership, rollback, and preserved failure causes. Added H2 commit/rollback/binding/concurrency tests and injected cleanup-failure tests.
- Added product/paging/account/address validation and meaningful success/denial/side-effect tests.
- Added typed not-found errors and API 404 mapping, synchronized Swagger contract/responses, and serialization tests.
- Restricted authentication redirects, escaped untrusted JSP values and flash text, and removed JavaScript flash interpolation.
- Introduced `StoreServices` as the composition root. Services inject repository interfaces; core has no HTTP, JSON, application, or infrastructure imports. Cart time is injectable.
- Renamed Java methods, accessors, fields, SQL aliases, DTO mapping keys, form IDs, and CSS hooks to English; renamed `PaginationDTO`. Added a JSP-import reference check after browser testing caught a stale import.
- Added V6 atomic `checkout_cart` with cart/stock locking, address ownership, decimal totals, duplicate-sale protection, and rollback. A real MySQL regression first reproduced partial stock loss, then passed with V6.
- Added V7 to rename the review table to `product_reviews` while preserving earlier migrations.
- Moved local Compose to MySQL 8.4.13 and a separate `mysql-data-v8` volume. Flyway Community 9.22.3 rejected the previous MySQL 5.7 baseline. Local binary logging is disabled for schema-scoped trigger migration.
- Replaced misleading client-only payment success with explicit demo checkout. Local Compose enables it; host launches require `ALLOW_DEMO_CHECKOUT=true`. No money is charged and no delivery is arranged.
- Expanded labels, review stars, demo shipping feedback, and BRL/date formatting across four locale bundles. Remaining titles, confirmations, placeholders, and server feedback are tracked rather than claimed complete.
- Added browser smoke and real customer/admin journeys, including escaped reviews, favorites, cart quantity, addresses, order/stock effects, logout token expiry, remembered-email login, admin catalog changes, and customer mutation denial.
- Added real-container API checks for guest/customer/admin access, own-account visibility, English payloads, hidden credentials, malformed IDs, and missing resources.
- Replaced numbered legacy test batches with six feature-focused classes and 33 deterministic database cases. Removed swallowed exceptions, random retry loops, vacuous empty price/history comparisons, and existence-only assertions. Cucumber has eight English scenarios with per-case database reset.
- Added JaCoCo reports and CI stages for migrations, all Java suites, browser demo modes, and report uploads. Updated shared Codex/Claude/Copilot meaningful-test and build-review workflows.
- Rewrote root README, specialty specs, database instructions, and backlog. Split account styles by responsibility; maintained source/docs files are below 300 lines.

## Verified results

- `mvn -B -Pmysql-integration clean verify`: passed 75 default tests and five MySQL integration cases; no failures, errors, or skips. It produced the WAR and servlet runner.
- `mvn -B -Plegacy-integration test`: passed 33 feature-focused database cases and eight Cucumber scenarios; no failures, errors, or skips. A subsequent default `verify` passed after final stylesheet changes.
- Browser suite: seven smoke cases plus two customer/admin journeys passed in each demo mode after the final API assertions (18 case executions total). Logs are `.local-tools/browser-enabled.log` and `.local-tools/browser-disabled.log`. Both runs include real-container API status/ownership checks.
- `python scripts/test_repository_contracts.py`: 12 checks passed, including JSP import references and inward core dependencies.
- `python scripts/validate_openapi.py`: passed with `scripts/requirements.txt` installed into `.local-tools/openapi`.
- Maintained Java/JSP/CSS/Python/JSON/SQL/Markdown files were scanned: none reached 300 lines. Applied historical migrations were preserved.
- Diff whitespace check passed with CRLF-aware whitespace settings.
- Combined instrumented Java coverage after default/MySQL/legacy runs: 1,241/2,149 lines (57.7%), 343/763 branches (45.0%). Browser processes are not instrumented. This is partial coverage; use branch gaps to choose meaningful next tests. The default-only report was 587/2,149 lines and 233/763 branches; do not confuse the scopes.

Reports live in `target/surefire-reports`, `target/failsafe-reports`, and `target/site/jacoco`. Maven logs are `.local-tools/mysql-build.log`, `.local-tools/legacy-tests.log`, and `.local-tools/build.log`. Browser server output is `target/browser-server.log`; it is overwritten by each runner launch. `mvn clean` removes target reports, so record evidence before cleaning.

## Resume on this workstation

Read `AGENTS.md`, the shared skills, this checkpoint, and `TODO.md`; inspect `git status` and current processes first. Do not reset or discard existing changes. Portable tools are ignored and should not be committed.

The disposable MySQL schema is exactly `e_store_integration`, configured on loopback port 3308. The temporary server was shut down cleanly after verification; browser-runner Tomcat processes also stopped. The schema-scoped account is `store_tests`. Its random private password is stored only in ignored `.local-tools/mysql-test-password.txt`. Never print or commit that value. Both Java database profiles refuse other schema names, then deliberately clean and reseed this schema per case.

If native MySQL is stopped, launch its portable server in the background with a hidden window from PowerShell:

```powershell
$mysqlRoot = Join-Path (Get-Location) '.local-tools/mysql-8.4.13-winx64'
Start-Process -WindowStyle Hidden -FilePath "$mysqlRoot/bin/mysqld.exe" -ArgumentList @(
  '--no-defaults', "--basedir=$mysqlRoot", "--datadir=$mysqlRoot/test-data",
  '--port=3308', '--mysqlx=0', '--skip-log-bin', '--bind-address=127.0.0.1'
)
```

Set the Java test environment in the same shell as Maven:

```powershell
$env:JAVA_HOME = (Get-ChildItem .local-tools -Directory -Filter 'jdk*' | Select-Object -First 1).FullName
$env:TEST_MYSQL_URL = 'jdbc:mysql://127.0.0.1:3308/e_store_integration?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
$env:TEST_MYSQL_USER = 'store_tests'
$env:TEST_MYSQL_PASSWORD = Get-Content .local-tools/mysql-test-password.txt -Raw
& .local-tools/apache-maven-3.9.9/bin/mvn.cmd -B -Pmysql-integration clean verify
& .local-tools/apache-maven-3.9.9/bin/mvn.cmd -B -Plegacy-integration test
& .local-tools/apache-maven-3.9.9/bin/mvn.cmd jacoco:report
```

Database suites and browser journeys share disposable fixtures: run them sequentially. For browser tests, after packaging:

```powershell
$env:STORE_JAVA = Join-Path $env:JAVA_HOME 'bin/java.exe'
$env:STORE_BROWSER_CHANNEL = 'msedge'
$env:PYTHONPATH = Join-Path (Get-Location) '.local-tools/python'
$env:STORE_DATABASE_E2E = '1'
$env:MYSQL_CONNECTION_STRING = $env:TEST_MYSQL_URL
$env:MYSQL_USER = $env:TEST_MYSQL_USER
$env:MYSQL_PASSWORD = $env:TEST_MYSQL_PASSWORD
$env:ALLOW_DEMO_CHECKOUT = 'true'
python scripts/run_browser_smoke.py
$env:ALLOW_DEMO_CHECKOUT = 'false'
python scripts/run_browser_smoke.py
```

The browser runner starts and stops its own Tomcat. A separate MySQL profile run resets browser fixture mutations. For OpenAPI checks, select `.local-tools/openapi` as `PYTHONPATH`; browser dependencies are in `.local-tools/python`.

## Blockers and remaining scope

Docker is unavailable. Native MySQL/Tomcat checks passed, but full Compose startup and hosted CI execution remain unverified. Production database adoption, dormant passwords, cart recovery/expiry, payment/fulfillment, and production operations require the decisions listed at the end of `TODO.md`.

Unblocked implementation remains: decimal Java money, typed row mapping, complete feedback localization, accessibility, remaining input/authorization branches, and runtime modernization. No claim is made that the entire backlog or all behavioral coverage is complete.
