package br.uff.store.application.i18n;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Locale;
import java.util.ResourceBundle;
import org.junit.Test;

public class LocalePreferencesTest {
    @Test
    public void supportsTheFourPublishedLocales() {
        assertTrue(LocalePreferences.supports("en"));
        assertTrue(LocalePreferences.supports("pt-BR"));
        assertTrue(LocalePreferences.supports("es"));
        assertTrue(LocalePreferences.supports("pt-PT"));
    }

    @Test
    public void rejectsUnpublishedAndMissingLocales() {
        assertFalse(LocalePreferences.supports("fr"));
        assertFalse(LocalePreferences.supports(null));
    }

    @Test
    public void resolvesOnlySupportedLocalesAndDefaultsToEnglish() {
        assertEquals(new Locale("pt", "BR"), LocalePreferences.resolve("pt-BR"));
        assertEquals(Locale.ENGLISH, LocalePreferences.resolve("fr"));
        assertEquals(Locale.ENGLISH, LocalePreferences.resolve(null));
    }

    @Test
    public void eachSupportedLocaleHasACompleteLanguageLabel() {
        assertEquals("Language", messageFor("en"));
        assertEquals("Idioma", messageFor("pt-BR"));
        assertEquals("Idioma", messageFor("pt-PT"));
        assertEquals("Idioma", messageFor("es"));
    }

    private String messageFor(String languageTag) {
        ResourceBundle messages = ResourceBundle.getBundle("messages", LocalePreferences.resolve(languageTag));
        return messages.getString("language.label");
    }
}
