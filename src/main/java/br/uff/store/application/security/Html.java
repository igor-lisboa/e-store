package br.uff.store.application.security;

/** Encode untrusted values for HTML text and quoted attributes, never for JavaScript. */
public final class Html {
    private Html() { }

    public static String escape(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString().replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
