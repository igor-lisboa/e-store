package br.uff.store.core.services;

import br.uff.store.core.exceptions.StoreException;

/** Shared boundary checks with messages that callers can present or translate. */
public final class InputRules {
    private InputRules() { }

    public static void positiveId(Integer value, String label) throws StoreException {
        if (value == null || value <= 0) {
            throw new StoreException(label + " must be a positive integer.");
        }
    }

    public static void requiredText(String value, int maxLength, String label) throws StoreException {
        if (value == null || value.trim().isEmpty() || value.length() > maxLength) {
            throw new StoreException(label + " is required and must not exceed " + maxLength + " characters.");
        }
    }

    public static void page(Integer size, Integer number) throws StoreException {
        if (size == null || size < 1 || size > 100 || number == null || number < 1) {
            throw new StoreException("Page size must be between 1 and 100 and page number must be positive.");
        }
        if ((long) (number - 1) * size > Integer.MAX_VALUE) {
            throw new StoreException("Requested page exceeds the supported range.");
        }
    }
}
