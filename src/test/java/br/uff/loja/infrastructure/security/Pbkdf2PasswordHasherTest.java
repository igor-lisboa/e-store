package br.uff.loja.infrastructure.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class Pbkdf2PasswordHasherTest {
    private final Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher();

    @Test
    public void hashesPasswordsWithUniqueSaltsAndVerifiesThem() {
        String firstHash = hasher.hash("correct horse battery staple");
        String secondHash = hasher.hash("correct horse battery staple");

        assertNotEquals(firstHash, secondHash);
        assertTrue(hasher.matches("correct horse battery staple", firstHash));
        assertFalse(hasher.matches("wrong password", firstHash));
        assertTrue(hasher.isEncoded(firstHash));
    }

    @Test
    public void acceptsLegacyPlaintextForOneTimeLoginMigration() {
        assertTrue(hasher.matches("legacy-password", "legacy-password"));
        assertFalse(hasher.matches("different-password", "legacy-password"));
        assertFalse(hasher.isEncoded("legacy-password"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsEmptyPasswords() {
        hasher.hash("");
    }

    @Test
    public void rejectsMalformedOrExcessivelyExpensiveStoredValues() {
        assertFalse(hasher.matches("password", "$pbkdf2-sha256$999999999$bad$value"));
        assertFalse(hasher.matches("password", "$pbkdf2-sha256$120000$bad"));
    }
}
