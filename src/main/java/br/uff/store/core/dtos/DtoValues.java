package br.uff.store.core.dtos;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.DateTimeException;
import java.util.Date;

/** Compatibility conversions for legacy map-backed DTOs; typed repository mapping is pending. */
final class DtoValues {
    private DtoValues() { }

    static Date date(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Date.from(LocalDateTime.parse(value.toString().replace(' ', 'T')).toInstant(ZoneOffset.UTC));
        } catch (DateTimeException invalid) {
            return null;
        }
    }

    static Integer optionalInteger(Object value) {
        if (value == null || "null".equals(value.toString())) {
            return null;
        }
        return Integer.valueOf(value.toString());
    }
}
