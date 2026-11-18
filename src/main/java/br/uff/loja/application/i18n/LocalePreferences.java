package br.uff.loja.application.i18n;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class LocalePreferences {
    private static final Set<String> SUPPORTED_TAGS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("en", "pt-BR", "es", "pt-PT")));

    private LocalePreferences() { }

    public static boolean supports(String languageTag) {
        return SUPPORTED_TAGS.contains(languageTag);
    }

    public static Locale resolve(String languageTag) {
        return supports(languageTag) ? Locale.forLanguageTag(languageTag) : Locale.ENGLISH;
    }
}
