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

Page titles, placeholders, and every JavaScript alert or confirmation are localized through `ui.pageTitle.*`, `ui.js.*` and related keys; a repository contract test rejects hard-coded versions, and a unit test verifies that strings placed inside JavaScript quotes contain no quote, backslash, or markup characters. Fixed server feedback (confirmations and `StoreException` sentences, 59 in all) is translated through the `flash` bundles by `FlashMessages`; a unit test scans the source and fails when a literal message lacks a translation in any locale. Sentences that embed values (cart or product IDs, limits, field names such as "Product ID must be a positive integer.") are matched against the templates in `FlashMessages.TEMPLATES`; the translation is stored under the template's key (for example `flash.unable.to.list.products.in.cart.id.0`), values are inserted unchanged, and field names are translated through their own keys. A source-scan test fails when a message built from an English prefix has no template, or a template or field name lacks a translation. A sentence that matches no template still appears in English. Catalog content and stored account/address data are not automatically translated. Property bundles must contain only ASCII and `\uXXXX` escapes because Java 8 reads them as ISO-8859-1; a test enforces this. ViaCEP uses Portuguese response keys because they are defined by that external service.

## Acceptance checks

- Unit tests cover the supported locale allowlist and fallback.
- Integration tests verify session persistence and unsupported values.
- Browser tests switch among all four locales and check representative page content.
