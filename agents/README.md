# AI-agnostic agent workflow

Shared contribution rules live in [`AGENTS.md`](../AGENTS.md), with a Claude Code entry point in [`CLAUDE.md`](../CLAUDE.md). The reusable `keep-project-current` skill is mirrored into Codex and Claude discovery directories; its canonical tool-independent instructions live at [`skills/keep-project-current/SKILL.md`](skills/keep-project-current/SKILL.md).

## Feature workflow

1. Read the relevant specialty spec, implementation, and backlog.
2. Gather requirements from behavior and tests. Mark assumptions and open decisions in the spec.
3. Plan a narrow behavior-preserving implementation.
4. Implement cohesive steps with inward dependency direction and files below 300 lines where practical.
5. Add or update unit, integration, and browser E2E tests at appropriate boundaries.
6. Update translations and user/developer documentation.
7. Run available checks, inspect the diff, update `TODO.md`, and report limitations honestly.

## Documentation rules

Specs record observed behavior, requirements, acceptance checks, and open questions. The backlog tracks incomplete implementation. Update the relevant spec and backlog in the same change as behavior changes. Do not silently delete requirements or mark work complete without evidence.
