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
- Shallow tests are not accepted. Each test must prove observable behavior or an important invariant and catch a plausible defect. Avoid trivial getters/setters, object-existence checks, assertions that merely repeat mock setup or implementation logic, and static wording checks presented as functional evidence.
- Follow `agents/skills/verify-quality/SKILL.md` (also exposed at `.github/skills/verify-quality/SKILL.md`) and `agents/test-build-reviewer.md`. Review relevant success/failure paths, boundaries, authorization, and side effects with deterministic isolated fixtures.
- Run required suites and `mvn -B clean verify`; inspect reports, skips, and packaging artifacts. Default tests do not include legacy integration tests. Never skip/weaken failures to obtain a passing result or claim unrun suites passed. Report missing tooling and coverage gaps, and cite coverage percentages only from actual reports.
- Use versioned migrations for tables, seeds, views, procedures, and triggers. Never use a database dump to initialize the application.
- Update specifications, documentation, and the backlog with code changes. Report checks actually run and any verification blockers.
- For API routes, inputs, serialized responses, validation, authentication/authorization, or errors, follow `agents/skills/maintain-openapi/SKILL.md` (also exposed at `.github/skills/maintain-openapi/SKILL.md`). Review with `agents/swagger-maintainer.md`, update Swagger in the same change, and run contract validation plus relevant request/serialization tests.
