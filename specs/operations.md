# Local operations specification

## Requirements

- Developers with Docker Compose can start the web application and MySQL from the repository root.
- The database initializes from a documented schema/seed process and reports healthy before the app starts.
- Application connection settings come from environment variables; secrets are excluded from version control.
- The app is reachable at `http://localhost:<MVN_PORT>` and logs are available through Compose.
- Persistent database data can be reset with an explicit documented command.

## Current implementation

Compose runs Maven/Java 8 and MySQL 8.4.13, publishes the app and database ports, and waits for database health before Flyway 9.22.3 applies migrations. The application starts after migrations succeed. Run `sh scripts/start.sh` from the repository root; the script preserves existing `.env` settings. Local Compose explicitly enables demonstration checkout without payment or fulfillment. This is a development environment; production deployment requirements remain open.

Native Java 8, Maven 3.9.9, MySQL 8.4.13, and Edge browser verification is recorded in `WORK_LOG.md`. Docker is unavailable on the current workstation, so complete Compose execution remains unverified. CI defines that workflow but its hosted execution must be checked separately.

## Acceptance checks

- A clean Compose startup builds, runs automated tests, initializes the database, and serves the app.
- A second startup preserves data; `docker compose down -v` removes this Compose project's managed database and Maven-cache volumes. It must only be used for disposable local data.
- The README documents prerequisites, configuration, commands, troubleshooting, and shutdown.

## Open questions

- Choose a supported production JDK and servlet runtime lifecycle; the current Java 8/Servlet 3.1 maintenance baseline requires a separate modernization plan.
