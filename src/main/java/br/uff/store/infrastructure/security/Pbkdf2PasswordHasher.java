package br.uff.store.infrastructure.security;

import br.uff.store.core.interfaces.services.IPasswordHasher;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class Pbkdf2PasswordHasher implements IPasswordHasher {
    private static final String PREFIX = "$pbkdf2-sha256$";
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 600000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;
    private static final int MAX_PASSWORD_LENGTH = 255;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String hash(String password) {
        requirePassword(password);
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] derivedKey = derive(password, salt, ITERATIONS);
        return PREFIX + ITERATIONS + "$" + encode(salt) + "$" + encode(derivedKey);
    }

    @Override
    public boolean matches(String candidate, String storedValue) {
        if (candidate == null || candidate.length() > MAX_PASSWORD_LENGTH || storedValue == null) {
            return false;
        }
        if (!isEncoded(storedValue)) {
            return MessageDigest.isEqual(bytes(candidate), bytes(storedValue));
        }
        try {
            String[] parts = storedValue.split("\\$", -1);
            int iterations = Integer.parseInt(parts[2]);
            byte[] salt = Base64.getUrlDecoder().decode(parts[3]);
            byte[] expected = Base64.getUrlDecoder().decode(parts[4]);
            if (iterations < ITERATIONS || iterations > 1200000
                    || salt.length < SALT_BYTES || expected.length != KEY_BITS / 8) {
                return false;
            }
            return MessageDigest.isEqual(expected, derive(candidate, salt, iterations));
        } catch (RuntimeException exception) {
            return false;
        }
    }

    @Override
    public boolean isEncoded(String storedValue) {
        return storedValue != null && storedValue.startsWith(PREFIX);
    }

    private byte[] derive(String password, byte[] salt, int iterations) {
        char[] passwordCharacters = password.toCharArray();
        PBEKeySpec keySpec = new PBEKeySpec(passwordCharacters, salt, iterations, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(keySpec).getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Password hashing is unavailable.", exception);
        } finally {
            keySpec.clearPassword();
            java.util.Arrays.fill(passwordCharacters, '\0');
        }
    }

    private static void requirePassword(String password) {
        if (password == null || password.isEmpty() || password.length() > MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must contain between 1 and 255 characters.");
        }
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private static String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }
}
