# Database migrations

Flyway tracks applied SQL migrations and checksums in `flyway_schema_history`. Compose starts MySQL, waits for health, runs the `migrate` service, and starts the application only after migration succeeds.

## Files

- `migrations/V1__create_tables.sql`: tables and foreign keys, ordered by dependency.
- `migrations/V2__create_catalog_view.sql`: catalog view without a hard-coded definer.
- `migrations/V3__create_checkout_procedure_and_stock_trigger.sql`: checkout procedure and stock guard.
- `migrations/V4__seed_reference_data.sql`: roles and English category names.
- `dev-seeds/V5__seed_development_store.sql`: local sample accounts and products.

Add a new `V<number>__description.sql` for each change. Never edit a migration already applied to a shared environment. SQL migrations are intentionally small and separated by responsibility. Flyway documentation describes [migration naming and execution](https://documentation.red-gate.com/fd/migrations-271585107.html).

## Local fixtures

Local Compose enables development seeds. The sample emails are `admin@example.test` and `customer@example.test`; both use `development-password`. Stored values use PBKDF2 hashes. These are public test accounts, never production accounts.

For environments without sample accounts, configure `FLYWAY_LOCATIONS=filesystem:/flyway/sql/schema` on the migration runner before its first run. Reference data is always required; development seed data is optional.

## Existing databases

Flyway deliberately refuses to migrate a non-empty schema without history. It never silently baselines an unknown database or restores a dump. Back up any valuable data first. For disposable local databases only, `docker compose down -v` removes local volumes so the next startup applies migrations to an empty database.

For a database that must be preserved, compare its schema and routines with V1–V3, reconcile differences, and explicitly baseline only after review. Existing seed IDs and categories may differ; plan V4 and development fixtures separately. Automatic adoption of an existing dump database is not implemented.

## Commands

```sh
docker compose run --rm migrate info
docker compose run --rm migrate validate
docker compose run --rm migrate migrate
```

MySQL DDL may commit implicitly. Resolve failed migrations explicitly after inspecting the schema; do not blindly repair history. The checkout routine is retained from the legacy schema and still needs transaction and concurrency testing.
