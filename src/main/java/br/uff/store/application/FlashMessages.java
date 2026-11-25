package br.uff.store.application;

import br.uff.store.core.exceptions.StoreException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    /**
     * Sentences that embed values (identifiers, limits, field names). {0}, {1} mark the embedded values; the
     * translation lives under the key of the template itself, for example "flash.unable.to.list.products.in.cart.id.0".
     */
    static final List<String> TEMPLATES = Collections.unmodifiableList(Arrays.asList(
            "{0} must be a positive integer.",
            "{0} is required and must not exceed {1} characters.",
            "{0} must be an uploaded image, a bundled img/ path, or an http(s) address.",
            "Unable to add product ID {0} in cart ID {1}.",
            "Product ID {0} is not in cart ID {1}.",
            "Unable to remove product ID {0} in cart ID {1}.",
            "Unable to list products in cart ID {0}.",
            "Unable to update quantity for product ID {0} in cart ID {1}.",
            "Stock increment must not exceed {0} units.",
            "Cart ID {0} is invalid, empty, sold, or owned by another user.",
            "Address ID {0} does not belong to cart owner ID {1}. Choose another address."));
    private static final List<Pattern> TEMPLATE_PATTERNS = compile(TEMPLATES);
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
        String translated = lookup(english, locale);
        if (translated != null) {
            return translated;
        }
        for (int index = 0; index < TEMPLATES.size(); index++) {
            Matcher matcher = TEMPLATE_PATTERNS.get(index).matcher(english);
            String pattern = lookup(TEMPLATES.get(index), locale);
            if (pattern != null && matcher.matches()) {
                Object[] values = new Object[matcher.groupCount()];
                for (int group = 0; group < values.length; group++) {
                    // Values are strings, so numbers are never reformatted; field names are translated when known.
                    String value = matcher.group(group + 1);
                    String translatedValue = lookup(value, locale);
                    values[group] = translatedValue == null ? value : translatedValue;
                }
                return MessageFormat.format(pattern, values);
            }
        }
        return english;
    }

    private static String lookup(String english, Locale locale) {
        try {
            return bundle(locale).getString(key(english));
        } catch (MissingResourceException untranslated) {
            return null;
        }
    }

    private static List<Pattern> compile(List<String> templates) {
        List<Pattern> patterns = new ArrayList<>();
        for (String template : templates) {
            StringBuilder regex = new StringBuilder();
            for (String part : template.split("(?<=\\})|(?=\\{)")) {
                regex.append(part.matches("\\{\\d+\\}") ? "(.+?)" : Pattern.quote(part));
            }
            patterns.add(Pattern.compile(regex.toString(), Pattern.DOTALL));
        }
        return patterns;
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
