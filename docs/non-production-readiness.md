# Not production ready: what to change before going live

This project is a learning/hobby store. It is built so the parts that talk to the outside world sit behind small
interfaces (ports) with demo adapters. Nothing below is safe or complete for real customers or real money. Each row
says what exists today and what to do to promote it. Keep this list current when an item changes.

## Replaceable adapters (swap in `StoreServices`, no core change)

| Area | Today (demo) | To promote to production |
|---|---|---|
| Payment | `DemoPaymentGateway` implements `IPaymentGateway`; no money moves, status `NOT_CHARGED`. `ALLOW_DEMO_CHECKOUT=true` enables it. | Pick a provider and write an adapter for `IPaymentGateway` (`authorize`, `release`). Store only provider references (the `payments` table is vendor neutral). Add idempotency keys, webhook confirmation, refunds on cancellation (cancelling an order does not refund today), and card-data scope review. Set `ALLOW_DEMO_CHECKOUT=false`. |
| Delivery | `DemoDeliveryGateway` implements `IDeliveryGateway`; fake method and no tracking. No screen moves an order to `SHIPPED`, so every order is cancellable. | Adapter for a carrier or shipping API, a fulfilment screen or webhook that sets `SHIPPED`, real tracking codes, and a cancellation rule tied to shipment. |
| Email | `FileEmailOutbox` implements `IEmailSender`; it only writes text files under `EMAIL_OUTBOX_DIR` (default: system temp folder). Nobody receives the password-reset email. | Adapter for SMTP or an email API; configure sender domain (SPF, DKIM, DMARC); templates per locale; bounce handling. |

## Settings that are unsafe by default

| Item | Today | To promote |
|---|---|---|
| Reset-link address | Without `PUBLIC_BASE_URL`, the reset link uses the request's host name. A forged `Host` header could aim a victim's link at another site. | Always set `PUBLIC_BASE_URL` to the real HTTPS address. |
| Database passwords | `.env.example` and Compose default to `123456` for the app and root accounts. | Use generated secrets from a secret manager, a least-privilege application account, and never default values. |
| Development seed accounts | Compose loads `admin@example.test` and `customer@example.test` with the public password `development-password`. | Production runs only versioned schema migrations; never load `database/dev-seeds`. |
| Transport security | webapp-runner serves plain HTTP. Session and CSRF cookies are not marked `Secure`. | Terminate TLS in front of the app, mark cookies `Secure` and `HttpOnly`, enable HSTS. |
| Browser hardening | No Content-Security-Policy, framing, or referrer headers. A third-party icon script loads from a CDN. | Add security headers, self-host assets, and pin script integrity. |
| Abuse controls | No rate limit or lockout on sign-in, registration, or password-reset requests; no CAPTCHA. | Add per-address and per-account throttling and monitoring. |
| Account recovery | Reset tokens last 60 minutes and are single use, but a request is accepted for any well-formed address. | Add request throttling and notify the owner when a password changes. |

## Platform and operations

| Area | Today | To promote |
|---|---|---|
| Runtime | Java 8 and Servlet 3.1 on webapp-runner (Tomcat), chosen for the original code base. | Move to a supported Java LTS and servlet container as a separately tested change (decision 14B). |
| Database | One MySQL container with a local volume. Flyway runs at startup of the Compose stack. | Managed or replicated database, tested backups and restores, retention rules, and a migration step in the release pipeline. |
| Observability | `java.util.logging` to the console only; no metrics, tracing, or alerts. Unexpected errors are logged and the user sees a generic sentence. | Structured logs shipped centrally, health and readiness endpoints, metrics, and alerting. |
| Audit and compliance | No audit trail for administrator actions; no data export or deletion flow; no privacy policy or terms. | Audit log, data-subject request handling, and legal pages for the target market. |
| Deployment | Local Docker Compose and a developer-run WAR. The hosted CI workflow was removed. | A build and release pipeline, environment configuration, and rollback plan (decision 12C keeps the project local-only for now). |

## How the swap works

1. Implement the port (`IPaymentGateway`, `IDeliveryGateway`, `IEmailSender`) in `infrastructure/gateways`.
2. Change the one line in `StoreServices` that creates the demo adapter.
3. Keep or extend the existing tests: the services are tested against the ports, so only the new adapter needs its own tests.
4. Remove the row from this file once the adapter is production grade and verified.
