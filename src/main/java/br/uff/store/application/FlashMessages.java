package br.uff.store.application;

import br.uff.store.core.exceptions.StoreException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpSession;

/**
 * Chooses the text shown to a visitor after a request. Messages written for users (StoreException and the
 * controllers' own confirmations) are looked up in the "flash" bundles by their English sentence, so each
 * locale sees its own language and an untranslated sentence falls back to English. Anything unexpected (SQL
 * errors, null pointers, library failures) is logged and replaced by a generic sentence so internal details
 * never reach the page.
 */
public final class FlashMessages {
    static final String GENERIC_FAILURE = "The request could not be completed.";
    private static final String BUNDLE = "flash";
    private static final String WELCOME = "Welcome, {0}!";
    private static final Logger LOGGER = Logger.getLogger(FlashMessages.class.getName());

    private FlashMessages() { }

    /** The visitor's chosen language, or English when none was chosen yet. */
    public static Locale localeOf(HttpSession session) {
        Object stored = session == null ? null : session.getAttribute("appLocale");
        return stored instanceof Locale ? (Locale) stored : Locale.ENGLISH;
    }

    public static String forFailure(Exception failure, Locale locale) {
        if (failure instanceof StoreException && failure.getMessage() != null) {
            return text(failure.getMessage(), locale);
        }
        LOGGER.log(Level.WARNING, "Request failed with an unexpected error.", failure);
        return text(GENERIC_FAILURE, locale);
    }

    public static String welcome(String name, Locale locale) {
        return MessageFormat.format(text(WELCOME, locale), name);
    }

    /** Returns the sentence in the visitor's language, or the English sentence when it has no translation. */
    public static String text(String english, Locale locale) {
        try {
            return bundle(locale).getString(key(english));
        } catch (MissingResourceException untranslated) {
            return english;
        }
    }

    /** Bundle key for an English sentence: lower-case words joined by dots, for example "flash.stock.increased". */
    public static String key(String english) {
        return "flash." + english.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", ".").replaceAll("^\\.|\\.$", "");
    }

    private static ResourceBundle bundle(Locale locale) {
        // No fallback to the server's default locale: an English visitor must never see Portuguese.
        return ResourceBundle.getBundle(BUNDLE, locale == null ? Locale.ENGLISH : locale,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
    }
}
