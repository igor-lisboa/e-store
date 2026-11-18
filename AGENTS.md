# Repository guidance for AI coding agents

- Read `README.md`, `TODO.md`, and the relevant specialty file under `specs/` before changing behavior.
- Use English for code, tests, comments, technical docs, and identifiers. User translations belong in message bundles.
- English naming is mandatory for every new or renamed file, directory, Java package, class, interface, enum, method, variable, test, asset, endpoint, and link. Do not introduce Portuguese or mixed-language identifiers. Update imports, build paths, templates, tests, and documentation together when renaming.
- Write source comments, logs, errors, and technical documentation in English. Keep Portuguese and Spanish only in translation resources and deliberately localized test fixtures.
- Preserve observed behavior unless a spec explicitly changes it. Record ambiguity instead of inventing policy.
- Keep changes cohesive and compatible with the current Java 8 / Servlet / JSP runtime until migration is planned.
- Prefer constructor injection and interfaces at boundaries. Keep domain/application rules independent of HTTP and JDBC.
- Follow SOLID and Clean Architecture while preferring simple, direct solutions a junior developer can understand. Avoid speculative abstractions and unnecessary layers.
- Use English for public endpoints, links, query parameters, and action names, and update every caller when changing them.
- Manage tables, views, procedures, triggers, and seed data through versioned migrations. Never restore a SQL dump as application startup.
- Aim for files under 300 lines. Split large files by responsibility while preserving behavior.
- Add or update unit, integration, and browser E2E coverage. Avoid shared mutable database fixtures.
- Update the specialty spec, translations, README, and backlog when their subject changes.
- For API changes, update the OpenAPI contract under `src/main/webapp/swagger/` and its schema/response files alongside serialization tests. Validate the specification before finishing.
- Apply `agents/skills/maintain-openapi/SKILL.md` whenever a change affects API routing, inputs, JSON serialization, validation, auth, or error responses. Use `agents/swagger-maintainer.md` as the API review checklist, including changes in services that alter endpoint behavior.
- Never commit `.env`, credentials, customer data, or production dumps.
- Inspect the diff, report checks actually run, and name blocked checks with reasons. Do not claim unverified results.
