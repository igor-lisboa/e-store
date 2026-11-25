package br.uff.store.application.i18n;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void bundlesUseOnlyAsciiAndValidUnicodeEscapesBecauseJavaReadsThemAsLatin1() throws Exception {
        for (String file : new String[] {"messages.properties", "messages_pt_BR.properties", "messages_es.properties", "messages_pt_PT.properties"}) {
            try (InputStream input = getClass().getClassLoader().getResourceAsStream(file)) {
                String text = new String(readAll(input), java.nio.charset.StandardCharsets.ISO_8859_1);
                for (char character : text.toCharArray()) {
                    assertTrue(file + " contains a non-ASCII character: " + character, character < 128);
                }
                assertFalse(file + " contains a \\x escape that Java does not decode", text.contains("\\x"));
            }
        }
    }

    private byte[] readAll(InputStream input) throws java.io.IOException {
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    @Test
    public void messagesPlacedInsideJavaScriptStringsCannotBreakOutOfTheirQuotes() throws Exception {
        for (String file : new String[] {"messages.properties", "messages_pt_BR.properties", "messages_es.properties", "messages_pt_PT.properties"}) {
            Properties bundle = load(file);
            for (String key : bundle.stringPropertyNames()) {
                if (key.startsWith("ui.js.") || key.startsWith("ui.confirm")) {
                    String value = bundle.getProperty(key);
                    for (char forbidden : new char[] {'\'', '"', '\\', '\n', '\r', '<', '>', '&'}) {
                        assertFalse(file + ": " + key + " contains " + forbidden, value.indexOf(forbidden) >= 0);
                    }
                }
            }
        }
    }

    @Test
    public void everyPageTitleKeyUsedByAPageExists() throws Exception {
        Properties english = load("messages.properties");
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("ui\\.pageTitle\\.\\w+").matcher("");
        for (java.io.File page : new java.io.File("src/main/webapp/pages").listFiles()) {
            String source = new String(java.nio.file.Files.readAllBytes(page.toPath()), java.nio.charset.StandardCharsets.UTF_8);
            matcher.reset(source);
            while (matcher.find()) {
                assertNotNull(page.getName() + " uses a missing key " + matcher.group(), english.getProperty(matcher.group()));
            }
        }
    }
}
