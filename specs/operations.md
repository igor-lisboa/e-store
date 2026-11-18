# Local operations specification

## Requirements

- Developers with Docker Compose can start the web application and MySQL from the repository root.
- The database initializes from a documented schema/seed process and reports healthy before the app starts.
- Application connection settings come from environment variables; secrets are excluded from version control.
- The app is reachable at `http://localhost:<MVN_PORT>` and logs are available through Compose.
- Persistent database data can be reset with an explicit documented command.

## Current implementation

Compose runs Maven/Java 8 and MySQL 5.7, publishes the app and database ports, and waits for database health before Flyway applies migrations. The application starts after migrations succeed. Run `sh scripts/start.sh` from the repository root; the script preserves existing `.env` settings. This is a development environment, not a hardened production deployment.

## Acceptance checks

- A clean Compose startup builds, runs automated tests, initializes the database, and serves the app.
- A second startup preserves data; the documented reset removes only the project database volume.
- The README documents prerequisites, configuration, commands, troubleshooting, and shutdown.

## Open questions

- Choose a supported JDK/MySQL baseline and production runtime after the Java EE 7 migration is planned.
