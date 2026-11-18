# Cart and checkout specification

## Observed behavior

- A cart can be created for an anonymous visitor using an IP address or for a signed-in user.
- A signed-in user can claim an eligible anonymous cart; carts are checked for existence, ownership, and sold state.
- Cart items can be added, removed, quantity-adjusted, listed, and paginated.
- Checkout requires a user, a valid active non-empty cart, and an address owned by that user.
- The database procedure `buy_cart_itens` records a sale and stock updates are guarded by database triggers in the seed dump.

## Requirements

- Cart ownership must be enforced on every read and mutation; IP address must not be treated as an authentication credential.
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

- Define cart expiry and whether anonymous carts merge into an existing account cart.
- Confirm payment-provider and order-cancellation scope; no payment integration is evident in the current source.
