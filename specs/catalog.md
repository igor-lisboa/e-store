# Product catalog specification

## Observed behavior

- The home page shows a limited set of products selected for the banner.
- The catalog supports product search, category and subcategory selection, special filters, and pagination.
- The servlet configures five items per page.
- Product administration supports listing, creation, updates, deletion, and stock changes.
- Product categories are stored in a parent/child hierarchy. The seed data has PlayStation, Xbox, and Wii roots with accessory, console, and game children.
- Product images are represented by a URL or data string in the current database model.

## Requirements

- Catalog browsing must not require authentication.
- Search, filters, and pagination must be composable and retain a stable ordering.
- Page size and page bounds must be validated by the application layer.
- Only authorized administrators may create, update, delete, or adjust product stock.
- Product name, price, description, category, and image input must be validated before persistence.
- Names/descriptions are required and bounded to 255 characters. Images are required and capped at 4,000,000 characters. Prices must be finite, non-negative, and fit `DECIMAL(11,2)`; stock increments are positive. Paging accepts 1–100 items with a positive page number and a non-overflowing offset. Price filter bounds must be finite, non-negative, and ordered.
- A product cannot be purchased beyond available stock.

## Acceptance checks

- Unit tests cover filter validation, paging boundaries, price and stock rules.
- Integration tests exercise catalog queries against a disposable MySQL database.
- Browser tests cover search, category selection, pagination, and an unauthorized administration attempt.

## Open questions

- Is the five-item page size a fixed product requirement or a current UI choice?
- Should out-of-stock products remain visible in the catalog?
