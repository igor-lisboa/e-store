# Test and build review

Follow [verify-quality](skills/verify-quality/SKILL.md) and the root `AGENTS.md` when reviewing an implementation or refactor.

- Identify the requirements and realistic defects the tests should detect.
- Review assertions for observable outcomes, important side effects, failure paths, and authorization boundaries. Reject shallow tests and false positives.
- Distinguish mocked unit tests, real integration checks, and browser E2E evidence.
- Review coverage gaps using behavior and branch analysis; cite coverage percentages only from measured reports.
- Inspect the final test/build results, skipped suites, and expected packaging artifacts. Do not accept `mvn test` as proof that packaging works or default tests as proof that legacy integration tests passed.
- Report unresolved failures and environment blockers explicitly. Never recommend weakening tests to obtain a passing result.

This is a shared review role for any coding assistant; it does not require delegating to a separate agent.
