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

- Confirm payment-provider and order-cancellation scope; no payment integration is evident in the current source.
- Cross-session anonymous cart recovery (signed opaque token) is not decided; expiry and merge are decided below.

## Decided requirements (pending implementation)

- Anonymous cart (decision 5C): on sign-in, the anonymous cart merges into the account cart. Anonymous carts expire after 30 days without activity (decision 2A, confirmed). Implemented by migration V8 (`carts.last_activity_at`) and `CartService.getActiveCart`: an expired anonymous cart is not reused or merged; every cart access records activity. Merge caps quantities at current stock in one transaction. Evidence: default unit tests, `CartActivityMySqlIT` against real MySQL (merge capping, rollback, expiry boundary), and the legacy suite; the browser journey is still pending (see the backlog). Cross-session recovery uses a signed opaque token only if it is added.
- Payment and delivery (decision 6): checkout depends on provider-agnostic ports in core. Each vendor is an adapter. Persisted payment and delivery data must be provider-neutral: provider name, external reference, status, amounts, delivery method, tracking code, address snapshot, and a reference to the raw vendor response. The local demo is the only adapter until a vendor is chosen.
- Cancellation (decision 7B): customers may cancel an order before fulfillment, and its stock is restored. Refunds apply only after a payment adapter exists.
- Order lines (decision 8A): each sale line stores the product name, unit price, and quantity at checkout, so order history does not change when product prices change.
