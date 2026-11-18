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

The session switch and message bundles exist. The shared header is partially localized. Other JSP text and server-side error messages remain Portuguese and are tracked in [the backlog](../TODO.md).

## Acceptance checks

- Unit tests cover the supported locale allowlist and fallback.
- Integration tests verify session persistence and unsupported values.
- Browser tests switch among all four locales and check representative page content.
