# e-store

A Java web storefront for browsing and searching products, managing an account, saving favorites, reviewing products, and completing purchases through a shopping cart.

The current application is a legacy Java 8, JSP/Servlet, Maven WAR project backed by MySQL. Its design is being improved in small, behavior-preserving steps. See the [specifications](specs/README.md) for observed behavior and target requirements, and [TODO.md](TODO.md) for known gaps.

## Features

- Product catalog with categories, search, filters, and pagination.
- Customer accounts, administrator role, and address management.
- Anonymous and account-owned carts, cart updates, and checkout.
- Product ratings and user favorites.
- Servlet JSON endpoints for products, users, and categories.
- Session language selection for English, Brazilian Portuguese, Spanish, and European Portuguese. Translation coverage is in progress.

## Requirements

- Docker Desktop or Docker Engine with the Docker Compose plugin.
- Ports `8181` and `3306` available by default. Configure different ports in `.env` if needed.

Docker supplies Maven and Java inside the application container, so a local JDK and Maven installation are not required for the recommended workflow.

## Start locally

From a POSIX shell (`sh`), run this single command from the repository root:

   ```sh
   sh scripts/start.sh
   ```

Then open [http://localhost:8181](http://localhost:8181).

Compose waits for MySQL health, runs Flyway migrations, builds the WAR, runs unit tests, and starts the web application. Migrations create the tables, catalog view, checkout procedure, stock trigger, and reference data. Local fixtures add sample accounts and products. The database volume persists across restarts.

Useful commands:

```sh
docker compose logs -f app database
docker compose down
docker compose down -v
docker compose run --rm app mvn -B verify
```

`docker compose down -v` deletes the development database volume. Use only non-sensitive local credentials in `.env`; `.env` is ignored by Git. Compose exposes MySQL on the configured host port for local database tools. See [database/README.md](database/README.md) for migrations, existing databases, and local seed accounts.

## Configuration

Copy `.env.example` to `.env` and change ports or local database credentials as needed. Compose configures the app to reach MySQL by its service name. For a Java process running directly on the host, use `jdbc:mysql://localhost:3306/e_store?useSSL=false&serverTimezone=UTC` as `MYSQL_CONNECTION_STRING` and provide `MYSQL_USER` and `MYSQL_PASSWORD`.

## Run without Docker

Install Java 8, Maven, MySQL, and Flyway 9.22.3. Run Flyway against an empty database using `database/migrations` as its location (also include `database/dev-seeds` for development fixtures). Copy `.env.local` to `.env` so the application loads the host database settings. Then run:

```sh
mvn clean verify
java -jar target/dependency/webapp-runner.jar --port 8181 target/e-store-1.0.1.war
```

This legacy runtime path is provided for maintenance; Docker Compose is the supported local setup.

## Tests and quality

```sh
mvn test
mvn verify
docker compose run --rm app mvn -B -Plegacy-integration test
python scripts/test_repository_contracts.py
python -m pip install -r scripts/requirements.txt
python scripts/validate_openapi.py
mvn org.pitest:pitest-maven:mutationCoverage
```

`mvn test` runs isolated tests. The `legacy-integration` profile runs the older database-backed tests and Cucumber features separately; they expect populated MySQL and mutate its data. They still need fixture isolation and reconciliation with current behavior. Do not point them at valuable data. Repository checks cover migration dependencies, seed hashes, view destinations, routes, and Java file sizes. CI runs unit tests and migration apply/validate/reapply checks. See [architecture and quality requirements](specs/architecture-quality.md) and [the tracked testing gaps](TODO.md).

## Repository map

```text
src/main/java/       Servlet endpoints, services, DTOs, and MySQL data access
src/main/webapp/     JSP pages, styles, and static images
src/main/resources/  Locale message bundles
src/test/            JUnit and Cucumber tests
database/            Versioned Flyway migrations, local seeds, and MySQL configuration
specs/               Feature and architecture specifications by specialty
agents/              AI-agnostic contribution workflow and skill
```

## API

Open [Swagger UI](http://localhost:8181/swagger/) after starting the app. The OpenAPI document is served at `/swagger/openapi.json`. Swagger assets are packaged in the WAR, so the documentation needs no external CDN at runtime. Sign in at `/account` in the same browser to try protected user endpoints; listing users requires an administrator. JSON response properties use English names such as `name`, `price`, `categoryId`, and `roleId`. See [specs/api.md](specs/api.md) for the contract and known gaps.

## Contributing

Use English for source code, comments, technical docs, and tests. Keep user-facing translations in locale bundles. For every behavior change, update the matching spec, add tests at the appropriate boundaries, and update the backlog. Aim to keep files below 300 lines. Read [AGENTS.md](AGENTS.md), [agents/README.md](agents/README.md), and the relevant specialty spec before making a change.
