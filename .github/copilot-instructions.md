# e-store contribution standards

Read and follow the root `AGENTS.md`, `README.md`, the relevant `specs/` files, and `TODO.md` before editing.

- Follow SOLID and Clean Architecture with dependencies pointing inward.
- Prefer simple code a junior developer can understand. Avoid unnecessary patterns, indirection, and speculative abstractions.
- Use English for code, comments, docs, endpoints, links, query parameters, and action values. Put translated interface text in locale resources.
- Use English filenames, directories, packages, classes, interfaces, enums, methods, variables, tests, and assets. Do not add Portuguese or mixed-language names. Rename all imports, templates, build paths, test references, and documentation together.
- Write logs and errors in English. Portuguese and Spanish are allowed only in translation resources and intentional locale fixtures.
- Keep files below 300 lines by splitting cohesive responsibilities.
- Use constructor injection, validate external input, and keep HTTP and database concerns outside business rules.
- Add meaningful unit, integration, and end-to-end coverage for changed behavior.
- Use versioned migrations for tables, seeds, views, procedures, and triggers. Never use a database dump to initialize the application.
- Update specifications, documentation, and the backlog with code changes. Report checks actually run and any verification blockers.
- For API routes, inputs, serialized responses, validation, authentication/authorization, or errors, follow `agents/skills/maintain-openapi/SKILL.md` (also exposed at `.github/skills/maintain-openapi/SKILL.md`). Review with `agents/swagger-maintainer.md`, update Swagger in the same change, and run contract validation plus relevant request/serialization tests.
