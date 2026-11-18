# Claude Code project guide

Use `.claude/skills/verify-quality/SKILL.md` and `agents/test-build-reviewer.md` for implementation/refactoring. Shallow tests are not accepted: every test must prove meaningful behavior or an important invariant and catch a plausible defect. Assert real outcomes, failure paths, boundaries, authorization, and side effects as applicable. Never weaken or skip failing tests to get a passing result. Run required suites and build packaging, distinguish default tests from legacy integration/E2E coverage, and report unverified checks and measured coverage honestly.

For API behavior changes, use `.claude/skills/maintain-openapi/SKILL.md` and the shared review role in `agents/swagger-maintainer.md`. Update OpenAPI with implementation and tests; report schema validation and runtime verification separately.

Use English consistently for filenames, directories, packages, classes, interfaces, enums, methods, variables, tests, comments, logs, errors, documentation, endpoints, and links. Do not introduce Portuguese or mixed-language names. Update all references when renaming. Localized Portuguese/Spanish text belongs in translation resources and intentional locale fixtures.

Follow SOLID and Clean Architecture. Prefer simple code a junior developer can understand, use good coding practices, and keep files below 300 lines. Use English endpoints and links. Manage schema, seeds, and routines through versioned migrations.

Follow the repository-wide instructions in [`AGENTS.md`](AGENTS.md). Read `README.md`, `TODO.md`, and the relevant specialty specification under `specs/` before changing behavior. Keep code and technical documentation in English, update specifications and tests with behavior changes, and report verification accurately.
