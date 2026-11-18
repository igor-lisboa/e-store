# HTTP API specification

## Observed behavior

- Servlet endpoints exist for products, users, and categories under `/api`.
- Product lookup serializes a product DTO as JSON. A Postman collection is included at the repository root.
- The current API implementation does not provide a consistent documented error schema or API versioning.

## Requirements

- Document each route, method, input, response, status code, authentication requirement, and error shape.
- Validate path and query parameters and return client errors for malformed input.
- Never serialize exception internals or stack traces to clients.
- Set JSON content type and UTF-8 consistently; use stable response DTOs.
- Apply the same authorization rules as the browser application.

## Acceptance checks

- Unit tests cover request validation and response mapping.
- Integration tests exercise routes with a real servlet container and disposable database.
- API contract tests validate the published Postman/OpenAPI examples against responses.

## Open questions

- Decide API versioning, authentication mechanism, and whether the existing Postman collection is the supported contract.
