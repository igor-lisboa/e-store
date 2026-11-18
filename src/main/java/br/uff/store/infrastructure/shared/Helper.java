package br.uff.store.infrastructure.shared;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Currency;
import java.text.NumberFormat;
import java.time.ZoneOffset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import br.uff.store.core.interfaces.shared.IHelper;

public class Helper implements IHelper {
    private final Locale locale;

    public Helper() {
        this(Locale.ENGLISH);
    }

    public Helper(Locale locale) {
        this.locale = locale == null ? Locale.ENGLISH : locale;
    }

    public Date convertStringToDate(String format, String date) {
        try {
            return new SimpleDateFormat(format).parse(date);
        } catch (Exception e) {
            return null;
        }
    }

    public String convertDateToString(String format, Date date) {
        try {
            return new SimpleDateFormat(format).format(date);
        } catch (Exception e) {
            return null;
        }
    }

    public Integer tryParseInteger(String intStr) {
        try {
            return Integer.valueOf(intStr);
        } catch (Exception ec) {
            return null;
        }
    }

    public String tryParseMoneyFormat(Object price) {
        NumberFormat format = NumberFormat.getCurrencyInstance(locale);
        format.setCurrency(Currency.getInstance("BRL"));
        return format.format(price);
    }

    public String formatDate(Date date) {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(locale)
                .withZone(ZoneOffset.UTC).format(date.toInstant());
    }

    public String formatReviewDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/uuuu"))
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale));
    }

    public BigDecimal tryParseDecimal(String intStr) {
        try {
            return new BigDecimal(intStr);
        } catch (Exception ec) {
            return null;
        }
    }
}
