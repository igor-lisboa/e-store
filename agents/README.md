# AI-agnostic agent workflow

Shared contribution rules live in [`AGENTS.md`](../AGENTS.md), with a Claude Code entry point in [`CLAUDE.md`](../CLAUDE.md). The reusable `keep-project-current` skill is mirrored into Codex and Claude discovery directories; its canonical tool-independent instructions live at [`skills/keep-project-current/SKILL.md`](skills/keep-project-current/SKILL.md).

GitHub Copilot follows [`.github/copilot-instructions.md`](../.github/copilot-instructions.md). All three tools share the same SOLID, simplicity, English naming, Clean Architecture, file-size, testing, and migration standards.

## Swagger maintenance

The [maintain-openapi skill](skills/maintain-openapi/SKILL.md) applies to API route, input, response, validation, authentication/authorization, and error changes. Discovery entry points live in `.agents/skills`, `.claude/skills`, and `.github/skills`. The shared [Swagger maintainer review role](swagger-maintainer.md) checks the implementation against the contract. Keep instructions in the canonical skill; the discovery files point to it.

OpenAPI validation runs in CI, but cannot prove runtime behavior matches the document. Request and serialization tests, plus runtime checks when available, provide that evidence.

## Feature workflow

Use the [verify-quality skill](skills/verify-quality/SKILL.md) and [test/build review role](test-build-reviewer.md) to plan meaningful coverage and verify suites and packaging. Codex, Claude, and Copilot discovery entry points live in their respective skill directories. Shallow tests are not accepted; every test must prove behavior or an important invariant. Report default unit, legacy integration, E2E, coverage, and build results with their actual scope and blockers.

1. Read the relevant specialty spec, implementation, and backlog.
2. Gather requirements from behavior and tests. Mark assumptions and open decisions in the spec.
3. Plan a narrow behavior-preserving implementation.
4. Implement cohesive steps with inward dependency direction and files below 300 lines where practical.
5. Add or update unit, integration, and browser E2E tests at appropriate boundaries.
6. Update translations and user/developer documentation.
7. Run available checks, inspect the diff, update `TODO.md`, and report limitations honestly.

## Documentation rules

Specs record observed behavior, requirements, acceptance checks, and open questions. The backlog tracks incomplete implementation. Update the relevant spec and backlog in the same change as behavior changes. Do not silently delete requirements or mark work complete without evidence.
