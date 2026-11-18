# Specifications

These documents describe observed storefront behavior and target quality requirements. “Observed” means visible in the current Java, JSP, servlet, SQL, or test sources; it is not a guarantee that every path works. Gaps and unverified behavior are tracked in [the backlog](../TODO.md).

## Specialties

- [Product catalog](catalog.md): browse, search, filter, pagination, administration, stock.
- [Accounts and addresses](accounts.md): registration, sign-in, roles, profiles, addresses.
- [Cart and checkout](cart-checkout.md): anonymous carts, ownership, quantities, purchases.
- [Ratings and favorites](ratings-favorites.md): product ratings and user favorites.
- [HTTP API](api.md): servlet endpoints and response expectations.
- [Persistence](persistence.md): MySQL schema, data access, transactions, configuration.
- [Localization](localization.md): supported language choices and translation coverage.
- [Operations](operations.md): local Compose and application runtime.
- [Architecture and quality](architecture-quality.md): boundaries, testing, maintainability requirements.

## Updating specifications

Update the relevant specialty specification in the same change as behavior or schema changes. Record unknowns as open questions; do not turn assumptions into requirements. See [the contributor agent guide](../agents/README.md) and the `keep-project-current` skill.
