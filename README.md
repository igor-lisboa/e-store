# e-store

A Java storefront with a searchable catalog, accounts, saved addresses, favorites, reviews, and shopping carts. Customers can place demonstration orders; administrators manage products and stock.

The application uses Java 8, Servlet 3.1/JSP, Maven WAR packaging, MySQL 8.4.13, and Flyway 9.22.3. It is being refactored incrementally with constructor injection and tested boundaries. The [specialty specifications](specs/README.md) describe behavior; [TODO.md](TODO.md) tracks gaps, and [WORK_LOG.md](WORK_LOG.md) provides a resumable development checkpoint.

## Start locally with one command

Install Docker Engine with Compose v2, or Docker Desktop. From a POSIX shell at the repository root:

```sh
sh scripts/start.sh
```

On Windows, use Git Bash or WSL with Docker integration. Open [http://localhost:8181](http://localhost:8181) after the build finishes. The script creates `.env` from `.env.example` if needed. Compose waits for MySQL health, applies migrations and local fixtures, runs default tests, builds the WAR, and starts Tomcat. Java and Maven are supplied by the container.

Default ports are 8181 for the store and 3306 for MySQL. Change `MVN_PORT` or `MYSQL_PORT` in `.env` if occupied. Configuration passwords are local development defaults; use independent secrets for any other environment.

| Local account | Password | Role |
| --- | --- | --- |
| `admin@example.test` | `development-password` | Administrator |
| `customer@example.test` | `development-password` | Customer |

These synthetic fixtures contain PBKDF2 password hashes. Local Compose explicitly enables `ALLOW_DEMO_CHECKOUT=true`: completing checkout records an order and reduces stock **without charging money or arranging delivery**. Direct host launches disable completion unless that variable is set to `true`. Real payment and fulfillment integrations remain pending.

```sh
docker compose logs -f app database
docker compose down
docker compose run --rm migrate validate
```

Password-reset emails are demo files: the Compose app writes them to `./outbox` (open the newest file for the link); nothing is sent. Everything that must change before real use is listed in [docs/non-production-readiness.md](docs/non-production-readiness.md). If port 3306 or 8181 is taken, set `MYSQL_PORT` or `MVN_PORT`.

The database persists in `mysql-data-v8`. For a disposable local reset, `docker compose down -v` deletes Compose-managed volumes, including database data and Maven cache. Start again to recreate fixtures. Existing MySQL 5.7 volumes are preserved under their old name; do not mount them directly into MySQL 8. Read [database/README.md](database/README.md) before adopting valuable data.

## Configuration and host development

Compose passes database settings through environment variables. For a host Java process, set `MYSQL_CONNECTION_STRING`, `MYSQL_USER`, and `MYSQL_PASSWORD`; for example:

```text
jdbc:mysql://127.0.0.1:3306/e_store?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
```

Install Java 8, Maven 3.9.9, MySQL 8.4.13, and Flyway 9.22.3. Migrate an empty schema using `database/migrations` and optionally `database/dev-seeds`. `.env.local` is a sample for host settings; copy it to ignored `.env` and adjust it. Environment variables take precedence. Then:

```sh
mvn -B clean verify
java -jar target/dependency/webapp-runner.jar --port 8181 target/e-store-1.0.1.war
```

Windows maintainers can optionally run `python scripts/bootstrap-local-tools.py` to download portable Java/Maven into ignored `.local-tools/`, then set `JAVA_HOME` and use the downloaded Maven executable. This does not provision MySQL or Docker.

## Troubleshooting

| Symptom | Check |
| --- | --- |
| Port already allocated | Change the relevant port in `.env`, then restart Compose. |
| Database connection refused during host development | Confirm the published MySQL port, JDBC host, schema, and environment variables. Compose uses `database`; host Java uses `127.0.0.1`. |
| Flyway refuses an existing schema | Follow the reviewed adoption procedure in `database/README.md`; do not enable automatic baselining for unknown data. |
| Form returns HTTP 403 after logout or expiry | Reload the form to obtain a token for the current session. |
| Checkout completion is unavailable | Host launches require explicit demo mode. Demo mode does not integrate payments. |
| Browser executable is missing | Run the documented Playwright install command or select an installed browser channel. |

## Tests and build verification

Default verification needs Java 8 and Maven:

```sh
mvn -B clean verify
python scripts/test_repository_contracts.py
python -m pip install -r scripts/requirements.txt
python scripts/validate_openapi.py
```

The default suite includes isolated service/servlet tests and H2 JDBC integration checks. It packages `target/e-store-1.0.1.war` and `target/dependency/webapp-runner.jar`; JaCoCo reports measured coverage in `target/site/jacoco/`. Passing default tests does not establish full project coverage. Meaningful behavioral assertions are required; accessor checks or assertions of configured mock returns are not sufficient.

Database suites deliberately clean and reseed a separate schema named exactly `e_store_integration` before each case. Create that schema and a schema-scoped test account, then export these variables in your shell:

```text
TEST_MYSQL_URL=jdbc:mysql://127.0.0.1:3306/e_store_integration?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
TEST_MYSQL_USER=your_test_user
TEST_MYSQL_PASSWORD=your_test_password
```

```sh
mvn -B -Pmysql-integration clean verify
mvn -B -Plegacy-integration test
```

The first profile runs default tests plus real MySQL migration/checkout checks: clean apply, validation, reapply, rollback, decimal totals, duplicate orders, ownership, and stock contention. The second runs the older database tests and English Cucumber scenarios with isolated fixtures. Both reject other schema names. These are destructive test commands: never provide production credentials.

For real browser checks, build first, then:

```sh
python -m pip install -r scripts/requirements-browser.txt
python -m playwright install chromium
python scripts/run_browser_smoke.py
```

The runner starts and stops its own local Tomcat. Smoke tests cover CSRF/session boundaries, all four locale selections, local redirect restrictions, Swagger rendering, and guest API denial. To include database shopping and administrator journeys, set `STORE_DATABASE_E2E=1`, point `MYSQL_CONNECTION_STRING`, `MYSQL_USER`, and `MYSQL_PASSWORD` to the disposable migrated test schema, and set `ALLOW_DEMO_CHECKOUT=true`. The runner then also runs the customer/administrator journeys (out-of-stock labels, review edit and delete, order cancellation, anonymous cart merge) and the hostile cases in `scripts/test_browser_hostile.py` (another customer or a guest touching someone else's review, address, or order) the keyboard-focus, contrast, and control-naming checks in `scripts/test_browser_accessibility.py`, and the password-recovery journey in `scripts/test_browser_recovery.py` (the runner points `EMAIL_OUTBOX_DIR` at a temporary folder). Repeat with `false` to verify forged completion cannot change stock. Run `python scripts/run_infrastructure_failure_check.py` after packaging to verify generic 500 responses with an unreachable database. These journeys add unique fixtures; reset with the MySQL profile between independent runs. `STORE_JAVA` selects a Java executable; `STORE_BROWSER_CHANNEL=msedge` optionally uses installed Edge.

The repository has no hosted CI workflow. Verification runs locally: repository and OpenAPI checks, migration apply/validate/reapply, default/MySQL/legacy suites, and both browser modes. The commands and results are recorded in [WORK_LOG.md](WORK_LOG.md).

## API and languages

Open [Swagger UI](http://localhost:8181/swagger/) after startup. Its OpenAPI 3.0.3 contract lives at `/swagger/openapi.json`, with local schema references and bundled UI assets. Sign in at `/account` in the same browser to try protected user endpoints; listing accounts requires an administrator. Product/account JSON uses English property names and omits passwords. See [specs/api.md](specs/api.md).

The header switches between English, Brazilian Portuguese, Spanish, and European Portuguese for the session. Shared page labels and locale-sensitive BRL/date formatting are implemented; remaining titles, confirmations, and dynamic feedback are tracked in [specs/localization.md](specs/localization.md).

## Repository and contribution workflow

| Location | Responsibility |
| --- | --- |
| `src/main/java/.../core` | Rules, service interfaces, DTOs; no HTTP/JDBC/framework imports |
| `src/main/java/.../application` | Composition root, servlets, security, API mapping |
| `src/main/java/.../infrastructure` | JDBC repositories, connection factory, formatting adapters |
| `src/main/webapp` | JSPs, static assets, Swagger contract |
| `src/test` | Unit, H2/MySQL integration, Cucumber coverage |
| `scripts` | Startup, repository/OpenAPI checks, browser runner |
| `database` | Versioned schema/routine migrations and optional development seeds |
| `specs` | Requirements and acceptance checks by specialty |
| `agents` | Shared AI-agnostic workflows and review roles |

Read [AGENTS.md](AGENTS.md), [agents/README.md](agents/README.md), and the relevant spec before changing behavior. Codex, Claude, and Copilot entrypoints refer to the same shared quality and Swagger skills. Use English names, endpoints, code, comments, tests, and technical docs; put user translations in bundles. Follow SOLID and Clean Architecture with straightforward code a junior developer can understand. Keep files below 300 lines, inject dependencies, and use new migrations instead of editing applied history.

Every change must update its specs and backlog, prove relevant success/failure and authorization behavior, and pass the affected test suites and build. Browser forms require their rendered session CSRF token; reload expired forms before retrying. Never weaken or skip a failing test merely to obtain a green build. Report measured coverage, unrun suites, and blockers honestly.
