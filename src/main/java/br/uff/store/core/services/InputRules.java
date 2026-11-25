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

    /**
     * Product images are shown through an img tag, so only the sources the admin form produces are accepted:
     * an uploaded data:image URI, a bundled img/ path, or an http(s) address. Anything else is rejected.
     */
    public static void imageSource(String value, String label) throws StoreException {
        String source = value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
        boolean allowed = source.startsWith("data:image/") || source.startsWith("img/")
                || source.startsWith("http://") || source.startsWith("https://");
        if (!allowed) {
            throw new StoreException(label + " must be an uploaded image, a bundled img/ path, or an http(s) address.");
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
