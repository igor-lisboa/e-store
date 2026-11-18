package br.uff.store.application.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.servlet.http.HttpSession;

/** A random capability bound to the browser session, never placed in a URL. */
public final class CsrfTokens {
    public static final String SESSION_ATTRIBUTE = "csrfToken";
    public static final String PARAMETER = "_csrf";
    private static final SecureRandom RANDOM = new SecureRandom();

    private CsrfTokens() { }

    public static String getOrCreate(HttpSession session) {
        synchronized (session) {
            Object existing = session.getAttribute(SESSION_ATTRIBUTE);
            if (existing instanceof String) {
                return (String) existing;
            }
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute(SESSION_ATTRIBUTE, token);
            return token;
        }
    }

    public static boolean matches(HttpSession session, String submitted) {
        if (session == null || submitted == null) {
            return false;
        }
        Object expected = session.getAttribute(SESSION_ATTRIBUTE);
        if (!(expected instanceof String)) {
            return false;
        }
        return MessageDigest.isEqual(((String) expected).getBytes(StandardCharsets.UTF_8),
                submitted.getBytes(StandardCharsets.UTF_8));
    }
}
