# Swagger maintainer review

Use this role for implementation or review of API changes. Follow [the maintain-openapi skill](skills/maintain-openapi/SKILL.md) and repository guidance in `AGENTS.md`.

Review the code diff together with the OpenAPI diff. Check servlet methods and paths, parameter validation, serialized properties, authentication/authorization branches, status codes, and shared errors. Identify undocumented operations and documented operations that no longer exist.

Completion requires an updated contract and appropriate response/request tests, or a clear explanation that the code change has no effect on the API contract. Report validation results separately from any runtime verification. Track unresolved contract/runtime mismatches in `TODO.md`; never conceal them by documenting desired behavior as implemented.
