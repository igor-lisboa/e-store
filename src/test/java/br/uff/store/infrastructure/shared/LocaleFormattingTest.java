package br.uff.store.infrastructure.shared;

import static org.junit.Assert.assertTrue;
import java.math.BigDecimal;
import java.util.Locale;
import org.junit.Test;

public class LocaleFormattingTest {
    @Test
    public void moneyUsesTheSelectedLocaleWithoutChangingTheCurrency() {
        BigDecimal amount = new BigDecimal("1234.50");
        String english = new Helper(Locale.ENGLISH).tryParseMoneyFormat(amount);
        String brazilian = new Helper(Locale.forLanguageTag("pt-BR")).tryParseMoneyFormat(amount);
        String spanish = new Helper(Locale.forLanguageTag("es")).tryParseMoneyFormat(amount);
        String portuguese = new Helper(Locale.forLanguageTag("pt-PT")).tryParseMoneyFormat(amount);
        assertTrue(english, english.contains("1,234.50"));
        assertTrue(brazilian, brazilian.contains("1.234,50"));
        assertTrue(spanish, spanish.contains("234,50"));
        assertTrue(portuguese, portuguese.contains("234,50"));
        assertTrue(english, english.contains("R$") || english.contains("BRL"));
        assertTrue(brazilian, brazilian.contains("R$"));
    }
}
