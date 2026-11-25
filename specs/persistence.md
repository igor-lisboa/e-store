# Persistence specification

## Observed behavior

- MySQL stores users, roles, addresses, products, categories, carts, cart items, sales, ratings, and favorites.
- `database/migrations` contains versioned tables, views, routines, and reference data. `database/dev-seeds` contains optional local fixtures. Flyway records applied versions and checksums.
- Data access classes issue SQL through `MySQLDAO`; DAOs map rows to typed DTOs through `RowMapper` in infrastructure; core DTOs never see a `Map`, and a repository contract test enforces that. Required columns that are missing fail loudly; optional ones map to null.
- Several operations wrap low-level failures in `StoreException`.
- Each DAO operation now owns its connection, statement, and result set through try-with-resources. A transaction uses one private connection, rolls back on failure, and retains rollback failures as suppressed exceptions. Repository cleanup no longer overrides the original error.
- H2-based JDBC integration tests verify commit, rollback, null mapping, and shared-DAO concurrent operations. These tests do not replace MySQL migration and stored-procedure verification.
- The `mysql-integration` profile applies, validates, and reapplies seven migrations against an explicitly disposable MySQL schema. It verifies atomic checkout rollback, decimal totals, duplicate-sale prevention, ownership, and stock contention.
- Legacy integration tests and Cucumber reset the disposable schema before each case. Both database profiles reject URLs whose schema is not exactly `e_store_integration`.
- V7 renames the historical misspelled review table to `product_reviews`. Earlier applied migrations remain unchanged.

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

- The local baseline is MySQL 8.4.13 and Flyway 9.22.3. Existing-database adoption still requires backup, schema comparison, and explicit review. The new Compose volume preserves any old MySQL 5.7 volume rather than opening it with a different server version.

## Decided requirements

- Production starts from the versioned migrations with no legacy data adoption (decision 3A).
- Money columns are `DECIMAL(11,2)`. Application code rounds prices half-up to two decimal places before persistence (decision 13B).
