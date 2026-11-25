# Product catalog specification

## Observed behavior

- The home page shows a limited set of products selected for the banner.
- The catalog supports product search, category and subcategory selection, special filters, and pagination.
- The storefront shows 20 items per page by default. An optional `pageSize` query parameter accepts 1–100; a missing or out-of-range value uses 20.
- Product administration supports listing, creation, updates, deletion, and stock changes.
- Product categories are stored in a parent/child hierarchy. The seed data has PlayStation, Xbox, and Wii roots with accessory, console, and game children.
- Product images are represented by a URL or data string in the current database model.

## Requirements

- Catalog browsing must not require authentication.
- Search, filters, and pagination must be composable and retain a stable ordering.
- Page size and page bounds must be validated by the application layer.
- Only authorized administrators may create, update, delete, or adjust product stock.
- Product name, price, description, category, and image input must be validated before persistence.
- Names/descriptions are required and bounded to 255 characters. Images are required and capped at 4,000,000 characters. Prices are `BigDecimal` amounts rounded half-up to two decimal places before persistence; the rounded value must be non-negative and at most 999,999,999.99 to fit `DECIMAL(11,2)`; stock increments are positive. Paging accepts 1–100 items with a positive page number and a non-overflowing offset. Price filter bounds must be non-negative and ordered.
- A product cannot be purchased beyond available stock.

## Acceptance checks

- Unit tests cover filter validation, paging boundaries, price and stock rules.
- Integration tests exercise catalog queries against a disposable MySQL database.
- Browser tests cover search, category selection, pagination, and an unauthorized administration attempt.

## Open questions

- Resolved: the page size is 20 by default with an optional `pageSize` parameter (decision 9C). Out-of-stock products stay visible and marked unavailable (decision 2A, implemented for the storefront and product page).

## Decided requirements (pending implementation)

- Single-product API (decision 1B): anonymous and customer callers see only whether a product is available. Administrators see the exact stock quantity. Implemented in `ProductApiServlet` with `PublicProductView`; evidence: servlet unit tests for anonymous, customer, and administrator callers and OpenAPI validation. Browser journeys read exact stock through a separate administrator session and confirm the public response has no `quantity`.
- Out-of-stock products (decision 2A): stay listed and show as "Out of stock" on the storefront listing; the product page disables add-to-cart. The featured home list intentionally keeps only in-stock products (promoting unavailable items is not useful). Evidence: `ProductListDTOTest`, `LegacyStoreIntegrationCatalogTest.storefrontListsOutOfStockProductsMarkedUnavailable`; the browser journey confirms the label, the disabled add-to-cart button, and their removal after stock is added.
