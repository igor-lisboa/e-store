# Cart and checkout specification

## Observed behavior

- A cart can be created for an anonymous visitor using an IP address or for a signed-in user.
- The active cart ID is held in the server-side session. Anonymous cart persistence across a new session is not guaranteed.
- A signed-in user can claim an eligible anonymous cart; carts are checked for existence, ownership, and sold state.
- Cart items can be added, removed, quantity-adjusted, listed, and paginated.
- Checkout requires a user, a valid active non-empty cart, and an address owned by that user.
- Migration V6 replaces the original procedure with `checkout_cart`. It locks the cart and stock rows, validates address ownership, deducts stock, calculates the decimal total, and records one sale inside a transaction. SQL failures roll back all changes.
- Order completion requires explicit `ALLOW_DEMO_CHECKOUT=true`. Local Compose enables this demonstration mode. It records an order without charging money; no payment provider is integrated. Forged completion requests are rejected when demo mode is disabled.
- MySQL integration tests cover rollback, decimal totals, duplicate checkout, address ownership, and competing carts. Browser journeys cover both demo-enabled and demo-disabled completion.

## Requirements

- Cart ownership must be enforced on every read and mutation; IP address must not be treated as an authentication credential.
- Cart IDs must not be accepted from a client-controlled sequential ID cookie. If cross-session anonymous recovery is required, use a signed opaque token or equivalent capability.
- Cart quantity changes must be positive and cannot exceed available stock at the time of the request.
- Quantities must be positive and bounded by current stock.
- Checkout must atomically validate stock, decrement inventory, create the sale, and close the cart.
- Retrying checkout must not create duplicate purchases.
- Prices used in the sale must be stable and auditable; use decimal money types rather than binary floating point.
- Failures must leave inventory and purchase records consistent.

## Acceptance checks

- Unit tests cover quantity, ownership, and checkout preconditions.
- Integration tests cover transaction rollback, stock contention, and duplicate checkout.
- Browser tests cover anonymous cart retention, sign-in handoff, quantity edits, and successful/failed checkout.

## Open questions

- Choose real payment and delivery vendors; only demonstration adapters exist.
- Cross-session anonymous cart recovery (signed opaque token) is not decided; expiry and merge are decided below.

## Decided requirements (pending implementation)

- Anonymous cart (decision 5C): on sign-in, the anonymous cart merges into the account cart. Anonymous carts expire after 30 days without activity (decision 2A, confirmed). Implemented by migration V8 (`carts.last_activity_at`) and `CartService.getActiveCart`: an expired anonymous cart is not reused or merged; every cart access records activity. Merge caps quantities at current stock in one transaction. Evidence: default unit tests, `CartActivityMySqlIT` against real MySQL (merge capping, rollback, expiry boundary), and the legacy suite; the browser journey passed (anonymous cart kept within the session, hidden from another visitor, merged into the account cart on sign-in). Cross-session recovery uses a signed opaque token only if it is added.
- Payment and delivery (decision 6, implemented): `SaleService` depends on the `IPaymentGateway` and `IDeliveryGateway` ports in core; vendors are adapters under `infrastructure/gateways`. The only adapters are `DemoPaymentGateway` and `DemoDeliveryGateway` (nothing is charged or shipped). The service authorizes payment for the cart total, arranges delivery, then calls the `place_order` procedure, which stores the sale, its lines, a `payments` row (provider, external reference, status, amount, raw-response reference) and a `deliveries` row (provider, method, tracking code, status, raw-response reference, and an address snapshot) in one transaction. If delivery or the database step fails, the payment authorization is released. Order history shows the snapshot address, so later address edits do not change past orders. Choosing real vendors is still open.
- Cancellation (decision 7B, implemented): the owner may cancel a `PLACED` order. `cancel_sale` locks the order, restores the stock of lines whose product still exists, and sets `CANCELLED` with a timestamp. Another customer's order looks missing, and `SHIPPED` or already cancelled orders are rejected. There is no refund because no payment adapter charges money yet; the shipping transition is not exposed in the UI, so every order is currently cancellable. The browser journey places an order, cancels it, and sees stock restored.
- Order lines (decision 8A, implemented): each sale line stores the product name, unit price, and quantity at checkout in `sale_lines`, with no foreign key to products, so edits or deletion do not change order history. Evidence: `SaleLifecycleMySqlIT` (snapshot, price/name change, product deletion, backfill) and the legacy suite.
