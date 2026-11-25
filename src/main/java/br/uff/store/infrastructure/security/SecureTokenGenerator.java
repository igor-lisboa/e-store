package br.uff.store.infrastructure.security;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.function.Supplier;

/** One-time tokens: 32 random bytes (256 bits) from the platform's secure generator, URL-safe encoded. */
public class SecureTokenGenerator implements Supplier<String> {
    private final SecureRandom random = new SecureRandom();

    @Override
    public String get() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
