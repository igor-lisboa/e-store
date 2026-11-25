package br.uff.store.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.uff.store.core.exceptions.StoreException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.servlet.http.HttpSession;
import org.junit.Test;

public class FlashMessagesTest {
    private static final Locale PORTUGUESE_BRAZIL = new Locale("pt", "BR");
    private static final Locale PORTUGUESE_PORTUGAL = new Locale("pt", "PT");
    private static final Locale SPANISH = new Locale("es");

    @Test
    public void userFacingMessagesAreShownUnchangedInEnglish() {
        assertEquals("Select an address.", FlashMessages.forFailure(new StoreException("Select an address."), Locale.ENGLISH));
    }

    @Test
    public void userFacingMessagesAreTranslatedForEachSupportedLocale() {
        StoreException failure = new StoreException("Invalid email or password.");

        assertEquals("E-mail ou senha inválidos.", FlashMessages.forFailure(failure, PORTUGUESE_BRAZIL));
        assertEquals("E-mail ou palavra-passe inválidos.", FlashMessages.forFailure(failure, PORTUGUESE_PORTUGAL));
        assertEquals("Correo electrónico o contraseña no válidos.", FlashMessages.forFailure(failure, SPANISH));
    }

    @Test
    public void englishVisitorsNeverGetAnotherLanguageEvenWhenTheServerDefaultLocaleIsPortuguese() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(PORTUGUESE_BRAZIL);
            assertEquals("Stock increased.", FlashMessages.text("Stock increased.", Locale.ENGLISH));
            assertEquals("Stock increased.", FlashMessages.text("Stock increased.", null));
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    public void sentencesWithoutATranslationFallBackToEnglish() {
        assertEquals("Cart ID 7 is invalid.", FlashMessages.text("Cart ID 7 is invalid.", PORTUGUESE_BRAZIL));
    }

    @Test
    public void storeExceptionsKeepTheirSafeMessageEvenWhenTheyWrapADatabaseError() {
        StoreException wrapped = new StoreException("Unable to complete the cart database operation.",
                new SQLException("Access denied for user 'root'@'10.0.0.5'"));

        String shown = FlashMessages.forFailure(wrapped, PORTUGUESE_BRAZIL);

        assertEquals("Não foi possível concluir a operação do carrinho.", shown);
        assertFalse(shown.contains("root"));
    }

    @Test
    public void unexpectedErrorsAreReplacedByAGenericSentenceInTheVisitorsLanguage() {
        assertEquals(FlashMessages.GENERIC_FAILURE, FlashMessages.forFailure(new SQLException("Table 'e_store.users' doesn't exist"), Locale.ENGLISH));
        assertEquals(FlashMessages.GENERIC_FAILURE, FlashMessages.forFailure(new NullPointerException(), null));
        assertEquals("No se pudo completar la solicitud.", FlashMessages.forFailure(new IllegalStateException("jdbc:mysql://db"), SPANISH));
    }

    @Test
    public void aStoreExceptionWithoutAMessageFallsBackToTheGenericSentence() {
        assertEquals(FlashMessages.GENERIC_FAILURE, FlashMessages.forFailure(new StoreException(null), Locale.ENGLISH));
    }

    @Test
    public void welcomeGreetsTheUserByNameInTheirLanguage() {
        assertEquals("Welcome, Ana!", FlashMessages.welcome("Ana", Locale.ENGLISH));
        assertEquals("¡Bienvenido, Ana!", FlashMessages.welcome("Ana", SPANISH));
        assertEquals("Bem-vindo, Ana!", FlashMessages.welcome("Ana", PORTUGUESE_PORTUGAL));
    }

    @Test
    public void sessionLocaleIsUsedWhenItIsALocaleAndEnglishOtherwise() {
        HttpSession session = mock(HttpSession.class);
        assertEquals(Locale.ENGLISH, FlashMessages.localeOf(null));
        assertEquals(Locale.ENGLISH, FlashMessages.localeOf(session));
        when(session.getAttribute("appLocale")).thenReturn("pt-BR");
        assertEquals(Locale.ENGLISH, FlashMessages.localeOf(session));
        when(session.getAttribute("appLocale")).thenReturn(PORTUGUESE_BRAZIL);
        assertEquals(PORTUGUESE_BRAZIL, FlashMessages.localeOf(session));
    }

    @Test
    public void keysAreStableLowerCaseDottedWords() {
        assertEquals("flash.stock.increased", FlashMessages.key("Stock increased."));
        assertEquals("flash.welcome.0", FlashMessages.key("Welcome, {0}!"));
    }

    @Test
    public void everyLiteralMessageInTheSourceHasATranslationInEveryLocale() throws IOException {
        List<String> literals = literalMessages();
        assertTrue("expected to find the application's user messages", literals.size() > 40);
        ResourceBundle.Control noFallback = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);
        for (Locale locale : new Locale[] {Locale.ENGLISH, SPANISH, PORTUGUESE_BRAZIL, PORTUGUESE_PORTUGAL}) {
            ResourceBundle bundle = ResourceBundle.getBundle("flash", locale, noFallback);
            for (String literal : literals) {
                assertTrue(locale + " lacks a translation for: " + literal, bundle.containsKey(FlashMessages.key(literal)));
            }
        }
    }

    @Test
    public void translationsDifferFromEnglishSoNothingIsLeftUntranslated() throws IOException {
        for (String literal : literalMessages()) {
            String english = FlashMessages.text(literal, Locale.ENGLISH);
            for (Locale locale : new Locale[] {SPANISH, PORTUGUESE_BRAZIL, PORTUGUESE_PORTUGAL}) {
                assertNotEquals(locale + " repeats the English sentence: " + literal, english, FlashMessages.text(literal, locale));
            }
        }
    }

    /** Fixed sentences the code can show: StoreException literals, controller confirmations, and the generic failure. */
    private static List<String> literalMessages() throws IOException {
        Pattern thrown = Pattern.compile("new StoreException\\(\"((?:[^\"\\\\]|\\\\.)*)\"\\s*[,)]");
        Pattern shown = Pattern.compile("FlashMessages\\.text\\(\"((?:[^\"\\\\]|\\\\.)*)\"");
        List<String> found = new ArrayList<>();
        found.add(FlashMessages.GENERIC_FAILURE);
        found.add("Welcome, {0}!");
        try (Stream<Path> files = Files.walk(Paths.get("src/main/java"))) {
            for (Path file : (Iterable<Path>) files.filter(path -> path.toString().endsWith(".java"))::iterator) {
                String source = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                for (Pattern pattern : new Pattern[] {thrown, shown}) {
                    Matcher matcher = pattern.matcher(source);
                    while (matcher.find()) {
                        if (!found.contains(matcher.group(1))) {
                            found.add(matcher.group(1));
                        }
                    }
                }
            }
        }
        return found;
    }
}
