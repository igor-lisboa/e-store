---
name: maintain-openapi
description: Keep e-store Swagger and OpenAPI documentation aligned when changing API routes, parameters, response serialization, validation, authentication, authorization, or error behavior; review API documentation for drift.
---

# Maintain OpenAPI

Use the repository root as the working directory. Read `specs/api.md`, the affected servlet and service flow, and the corresponding serialization/request tests. The contract is `src/main/webapp/swagger/openapi.json`; reusable schemas and responses live in its `schemas/` and `responses/` directories.

## Update with the implementation

- For each changed operation, reconcile the HTTP method, path, parameter names/types, required values, bounds, success payload, and every reachable error status with the actual handler.
- Inspect Gson output, including `@SerializedName`, omitted null fields, and transient credentials. Describe the JSON sent on the wire rather than Java field or getter names. Never document passwords or hashes as account response fields.
- Reflect session authentication and ownership/admin checks per operation. Swagger UI reuses the same-browser session established at `/account`; do not suggest entering cookie values in the Authorize dialog.
- Keep operation IDs unique and all descriptions, public names, and examples in English. Use synthetic examples without credentials or personal data.
- Document current behavior, including known limitations. Do not claim a 404, pagination, write endpoint, or authentication scheme that the handler does not implement. Record intended changes separately in `specs/api.md` and `TODO.md`.
- When removing or renaming routes, remove stale operations and references, update callers and tests, and describe compatibility changes in the README/API spec.
- Keep files below 300 lines by using local schema/response references. Preserve relative URLs so Swagger works when deployed under a servlet context path.

## Check for drift

Compare the affected `@WebServlet` mappings and supported methods against documented operations in both directions. Trace shared error handling and authorization branches, not just the successful path. Update serialization tests and request tests for observable changes. If the endpoint inventory changes, update the expected route inventory in `scripts/test_repository_contracts.py`.

Run the relevant checks:

```sh
python -m pip install -r scripts/requirements.txt
python scripts/validate_openapi.py
python scripts/test_repository_contracts.py
mvn test
```

Schema validation proves the document is valid, not that it matches runtime behavior. When a running application is available, load `/swagger/`, confirm schemas resolve, and exercise the changed operation with representative success, invalid-input, guest, owner, and administrator cases as applicable. Use disposable fixtures for mutating requests.

Before finishing, update `specs/api.md`, README, and backlog where affected. Report contract files changed, checks actually run, runtime cases exercised, and blockers. Do not claim Swagger is current solely because validation passed.
