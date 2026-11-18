# Ratings and favorites specification

## Observed behavior

- Product ratings are associated with a user and product in `user_produts_rating`.
- Favorite products are stored per user in `favorite_products`.
- Services expose rating and favorite operations, including toggling a favorite.
- Product display DTOs contain rating and favorite-related data.

## Requirements

- A rating must be within a documented range and have an explicit policy for repeat ratings.
- Only an authenticated user may create or update their rating or favorite list.
- Ratings are limited to 1–5; review titles are required, and review text must fit the database columns.
- Review submissions are processed only by POST requests and derive the user ID from the authenticated session.
- Users may change only their own rating and favorite state.
- Aggregated rating values must be derived consistently and safely when no ratings exist.

## Acceptance checks

- Unit tests cover rating bounds, repeat-rating behavior, and favorite toggle behavior.
- Integration tests verify user/product relationships and aggregate calculations.
- Browser tests cover rating and favorite actions as a signed-in user and access denial as a guest.

## Open questions

- Review edits are not supported; confirm whether users should be able to change a review.
