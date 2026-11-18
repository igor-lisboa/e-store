# Persistence specification

## Observed behavior

- MySQL stores users, roles, addresses, products, categories, carts, cart items, sales, ratings, and favorites.
- `database/migrations` contains versioned tables, views, routines, and reference data. `database/dev-seeds` contains optional local fixtures. Flyway records applied versions and checksums.
- Data access classes issue SQL through `MySQLDAO`; DAOs return map-backed DTOs.
- Several operations wrap low-level failures in `StoreException`.

## Requirements

- SQL values must always be bound parameters. Dynamic SQL fragments must come from validated allowlists.
- Connections, statements, and result sets must close deterministically, including on exceptions.
- Transaction ownership must be explicit and rollback failures must not hide the original failure.
- Schema changes must be versioned, repeatable migrations; seed data must be separate from production migrations.
- Store monetary amounts as `DECIMAL` and passwords as one-way hashes.
- Tests must use disposable databases or isolated schemas and must not depend on shared mutable fixtures.

## Acceptance checks

- Integration tests apply migrations to an empty database and exercise all repositories.
- Failure-path tests verify rollback and resource cleanup.
- Static review confirms parameterized queries and no production credentials in source control.

## Open questions

- MySQL 5.7 and Flyway 9.22.3 are the current local baseline. Plan a supported MySQL upgrade and verify existing-database adoption before production use.
