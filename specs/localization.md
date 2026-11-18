# Localization specification

## Required locales

- English (`en`), the fallback locale.
- Brazilian Portuguese (`pt-BR`).
- Spanish (`es`).
- European Portuguese (`pt-PT`).

## Requirements

- A visitor can select one of the supported locales; the preference persists for the session.
- Unsupported locale values are ignored and cannot create arbitrary bundle lookups.
- All user-visible text, validation feedback, dates, and currency formatting must use the selected locale.
- Locale choice must be accessible by keyboard and have a programmatic label.
- Translation keys must exist in every locale bundle, with English as the source language.

## Current implementation scope

The session switch, header, and 94 shared page labels use message bundles for all four locales. Currency remains BRL and is formatted using the selected locale. Order and review dates use localized formatting with UTC as the explicit date-time zone. Tests verify complete bundle keys, representative currency/date output, and sign-in labels in all four locales through a real browser.

Page titles, JavaScript confirmations, placeholders, and dynamic server feedback still need complete bundle coverage. Catalog content and stored account/address data are not automatically translated. ViaCEP uses Portuguese response keys because they are defined by that external service.

## Acceptance checks

- Unit tests cover the supported locale allowlist and fallback.
- Integration tests verify session persistence and unsupported values.
- Browser tests switch among all four locales and check representative page content.
