package br.uff.store.application.i18n;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.Properties;
import org.junit.Test;

public class TranslationBundleTest {
    @Test
    public void everySupportedLocaleExplicitlyDefinesEveryMessage() throws Exception {
        Properties english = load("messages.properties");
        for (String file : new String[] {"messages_pt_BR.properties", "messages_es.properties", "messages_pt_PT.properties"}) {
            Properties translated = load(file);
            assertEquals(file, english.stringPropertyNames(), translated.stringPropertyNames());
            for (String key : translated.stringPropertyNames()) {
                assertFalse(file + ": " + key, translated.getProperty(key).trim().isEmpty());
            }
        }
    }

    private Properties load(String name) throws Exception {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(name)) {
            assertNotNull(name, input);
            Properties properties = new Properties();
            properties.load(input);
            return properties;
        }
    }
}
