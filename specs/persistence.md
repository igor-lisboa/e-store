# Persistence specification

## Observed behavior

- MySQL stores users, roles, addresses, products, categories, carts, cart items, sales, ratings, and favorites.
- `database/dump/dump.sql` is both schema and sample data, with stored procedures and triggers.
- Data access classes issue SQL through `MySQLDAO`; DAOs return map-backed DTOs.
- Several operations wrap low-level failures in `LojaException`.

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

- Define supported MySQL version and a migration tool before replacing the checked-in dump workflow.
