# Accounts and addresses specification

## Observed behavior

- Users are persisted with name, email, password, and role fields.
- The account servlet handles account operations and uses session attributes for the selected user and role.
- Address records are associated with a user and include postal code, street, city, state, and country.
- Checkout validates that the selected address belongs to the purchasing user.

## Requirements

- Passwords must be stored using a modern adaptive one-way password hash.
- Legacy plaintext credentials are accepted only during login and upgraded to PBKDF2 on successful authentication; new and changed passwords are hashed before storage.
- Passwords and password hashes must never be returned in API JSON, rendered in account forms, or persisted in browser cookies.
- Authentication state and role checks must be enforced server-side for every protected action.
- Users may view or edit only their own account and addresses, except for explicitly authorized administration.
- Email and address fields must be validated; error messages must not expose secrets or SQL details.
- Session identifiers must be rotated after successful authentication.
- Post-login redirects are restricted to known local storefront routes. Submitted redirect values must never become arbitrary URLs or HTML.
- Account names/emails are required and bounded to the schema length; email syntax, role IDs, and new-account passwords are validated before database access. Address fields and owner IDs are validated before writes.
- Sign-in, registration, account edits, and sign-out require a CSRF token from the current browser session. The token stays valid after session-ID rotation and expires when the session is invalidated.

## Security changes implemented

- Login rotates the session identifier, and the remember-me behavior stores only the email address.
- Account updates require the signed-in user to own the target account or have the administrator role. Non-administrators cannot change their role.
- Address read-for-edit, update, and delete operations verify that the address belongs to the signed-in user.
- User registration always creates a new customer account, regardless of submitted account-selection state.
- The user service hashes passwords with PBKDF2-HMAC-SHA256 using a 600,000 iteration work factor and a unique random salt, following the current [OWASP password-storage guidance](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html). Legacy plaintext passwords upgrade after successful login; returned account DTOs clear password data.
- Password inputs are limited to 255 characters, matching the legacy account field limit; final password policy remains open.

## Acceptance checks

- Unit tests cover credential validation and authorization rules.
- Integration tests cover account persistence, authentication, address ownership, and access denial.
- Browser tests cover sign-in, sign-out, account editing, and address management.

## Open questions

- Registration is covered by browser tests; password recovery is specified below.
- Define supported password policy and account recovery requirements before implementation.

## Decided requirements

- Passwords set on account creation or replaced on edit must have at least 8 characters (decision 11A). No other composition rules apply. Accounts that keep their password on edit are not re-checked, and existing hashes are not rewritten by this rule.
- Password recovery (decision 4A) works through the `/password-reset` page. A visitor enters an email address and always sees the same answer, whether or not the account exists. For a known account, a random 256-bit token is created; only its SHA-256 hash is stored (`password_reset_tokens`, V12) with a 60-minute expiry, and any earlier unused token of that account is discarded. The emailed link opens a form that needs a new password of at least 8 characters; a rejected password does not use up the link. A token works once (a conditional update makes simultaneous uses race safely), cannot reset any other account, and never works after it expires.
- An account whose stored password is not a PBKDF2 hash (a legacy value) can no longer sign in, even with the right password: the attempt is refused with a message and a reset email is sent. The legacy value is not silently upgraded.
- Email goes through the `IEmailSender` port. The only adapter, `FileEmailOutbox`, writes files to `EMAIL_OUTBOX_DIR` and is not production ready (see `docs/non-production-readiness.md`). `PUBLIC_BASE_URL` sets the address used in links; without it the request's own address is used, which is only acceptable for local demos.
