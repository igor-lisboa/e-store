---
name: keep-project-current
description: Keep project specifications, tests, localization, architecture, and backlog aligned with code while implementing or refactoring a feature.
---

# Keep project current

Use this tool-independent workflow for feature implementation, refactoring, and maintenance. Codex, Claude Code, and other agents can follow these instructions.

## Before changing code

- Read root agent guidance, README, backlog, and relevant specialty specifications.
- Inspect the full affected flow from HTTP/UI through application services to persistence and tests.
- Distinguish observed behavior from target requirements. Record unknowns instead of inventing policy.
- Identify security, data migration, compatibility, and locale impacts.

## During implementation

- Make cohesive changes and preserve behavior unless the spec requires a change.
- Keep dependencies pointing toward domain and application rules; isolate frameworks and persistence at the edges.
- Prefer explicit dependencies, validation, deterministic errors, and files under 300 lines.
- Write English source and technical docs. Put user-visible translations in locale resources and update every supported locale.
- Add unit tests for rules, integration tests for adapters/persistence, and browser E2E tests for key journeys.
- Keep tests independent of shared mutable data and make failures actionable.

## Before finishing

- Update the relevant spec with observed behavior, requirements, acceptance checks, and unresolved questions.
- Update `TODO.md` only when completion is evidenced; add newly found gaps.
- Update README/setup/API docs when commands or interfaces change.
- Run available formatting, static, unit, integration, and E2E checks.
- Review the diff for secrets, generated files, unrelated changes, and files over the line target.
- Report changes, verified checks, and checks that could not run with concrete reasons.
