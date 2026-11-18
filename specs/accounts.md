# Accounts and addresses specification

## Observed behavior

- Users are persisted with name, email, password, and role fields.
- The account servlet handles account operations and uses session attributes for the selected user and role.
- Address records are associated with a user and include postal code, street, city, state, and country.
- Checkout validates that the selected address belongs to the purchasing user.

## Requirements

- Passwords must be stored using a modern adaptive one-way password hash.
- Authentication state and role checks must be enforced server-side for every protected action.
- Users may view or edit only their own account and addresses, except for explicitly authorized administration.
- Email and address fields must be validated; error messages must not expose secrets or SQL details.
- Session identifiers must be rotated after successful authentication.

## Acceptance checks

- Unit tests cover credential validation and authorization rules.
- Integration tests cover account persistence, authentication, address ownership, and access denial.
- Browser tests cover sign-in, sign-out, account editing, and address management.

## Open questions

- Registration and password-reset flows are not clearly specified by the current tests.
- Define supported password policy and account recovery requirements before implementation.
