# HTTP API specification

## Observed behavior

- Servlet endpoints exist for products, users, and categories under `/api`.
- Product lookup serializes a product DTO as JSON. Swagger UI is served at `/swagger/` and the OpenAPI 3.0.3 contract at `/swagger/openapi.json`.
- `GET /api/products/{productId}` returns availability only to anonymous and customer callers; administrators receive exact quantity (decision 1B). Non-admin responses use the `PublicProduct` schema (`available` boolean, no `quantity`).
- `GET /api/products/` lists every product with stock quantities and descriptions; it requires an administrator session (401 when signed out, 403 for customers). Single-product lookup stays public but hides the stock count from non-administrators.
- User API errors use a generic JSON error object and do not serialize exceptions.
- All API error responses use an `error` string. Product/account lookups return 404 for absent records, while infrastructure failures remain generic 500 errors. `scripts/run_infrastructure_failure_check.py` starts the packaged WAR with an unreachable database and verifies that the API answers 500 with only the generic error, requests that need no database still succeed, and pages show no stack traces or JDBC details. API versioning remains pending.
- JSON properties are English, including product `name`, `price`, `description`, `image`, `categoryId`, `quantity`, `category`, and account `name`, `email`, `roleId`. Java DTO fields and accessors also use English names.
- Category responses retain the `id` and `name` shape. The platform hierarchy now exposes `Nintendo`, including labels such as `Nintendo > Games`, instead of the legacy `Wii` value.
- Swagger UI is bundled as a WebJar and loads the local OpenAPI document; it does not depend on a runtime CDN. The setup follows the [Swagger UI installation guide](https://swagger.io/docs/open-source-tools/swagger-ui/usage/installation/).

## Requirements

Public routes use English. The API routes are `/api/products/*`, `/api/categories/*`, and `/api/users/*`. Browser routes are `/`, `/products`, `/product`, `/product-admin`, `/account`, `/addresses`, `/cart`, `/checkout`, `/reviews`, and `/language`. Legacy Portuguese route names are removed. Clients must update bookmarked URLs and API requests.

Browser parameters include `productId`, `addProductId`, `quantity`, `search`, `selectedId`, `addressId`, `page`, and `history`. Action values include `save`, `delete`, `register`, `addStock`, `submitReview`, `confirmCart`, `continueShopping`, `removeProduct`, `changeQuantity`, `continuePayment`, `paymentSuccess`, and `paymentFailure`.

- Document each route, method, input, response, status code, authentication requirement, and error shape.
- Validate path and query parameters and return client errors for malformed input.
- Never serialize exception internals or stack traces to clients.
- Set JSON content type and UTF-8 consistently; use stable response DTOs.
- Apply the same authorization rules as the browser application.

## Acceptance checks

- Unit tests cover request validation and response mapping.
- Integration tests exercise routes with a real servlet container and disposable database.
- API contract tests validate the OpenAPI document and its references, with response serialization tests for English JSON properties.

## Open questions

- Decide API versioning. OpenAPI is the supported API documentation; session authentication is currently required for user endpoints.
