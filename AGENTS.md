# Repository guidance for AI coding agents

- Read `README.md`, `TODO.md`, and the relevant specialty file under `specs/` before changing behavior.
- Use English for code, tests, comments, technical docs, and identifiers. User translations belong in message bundles.
- Preserve observed behavior unless a spec explicitly changes it. Record ambiguity instead of inventing policy.
- Keep changes cohesive and compatible with the current Java 8 / Servlet / JSP runtime until migration is planned.
- Prefer constructor injection and interfaces at boundaries. Keep domain/application rules independent of HTTP and JDBC.
- Aim for files under 300 lines. Split large files by responsibility while preserving behavior.
- Add or update unit, integration, and browser E2E coverage. Avoid shared mutable database fixtures.
- Update the specialty spec, translations, README, and backlog when their subject changes.
- Never commit `.env`, credentials, customer data, or production dumps.
- Inspect the diff, report checks actually run, and name blocked checks with reasons. Do not claim unverified results.
