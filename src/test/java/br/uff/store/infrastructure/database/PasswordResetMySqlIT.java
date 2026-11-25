package br.uff.store.infrastructure.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import br.uff.store.core.exceptions.PasswordResetRequiredException;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.services.PasswordResetService;
import br.uff.store.core.services.UserService;
import br.uff.store.infrastructure.data.PasswordResetData;
import br.uff.store.infrastructure.data.UserData;
import br.uff.store.infrastructure.security.Pbkdf2PasswordHasher;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Password recovery (decision 4A) against the migrated MySQL schema: single use, expiry, and legacy accounts. */
public class PasswordResetMySqlIT {
    private Connection connection;
    private PasswordResetData resetData;
    private UserData userData;
    private PasswordResetService service;
    private UserService users;
    private final List<String> sentBodies = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        MySqlTestDatabase.reset();
        String url = System.getenv("TEST_MYSQL_URL");
        String user = System.getenv("TEST_MYSQL_USER");
        String password = System.getenv("TEST_MYSQL_PASSWORD");
        connection = DriverManager.getConnection(url, user, password);
        MySQLDAO dao = new MySQLDAO(() -> DriverManager.getConnection(url, user, password));
        resetData = new PasswordResetData(dao);
        userData = new UserData(dao);
        Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher();
        int[] counter = {0};
        service = new PasswordResetService(userData, resetData, hasher,
                (to, subject, body) -> sentBodies.add(body), () -> "token-" + (++counter[0]));
        users = new UserService(userData, hasher);
    }

    @After
    public void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void aTokenResetsThePasswordExactlyOnceAndTheNewPasswordSignsIn() throws Exception {
        service.requestReset("customer@example.test", "http://x/password-reset");

        service.resetPassword("token-1", "a-brand-new-password");

        assertEquals(2, (int) users.login("customer@example.test", "a-brand-new-password").getId());
        try {
            service.resetPassword("token-1", "another-password-again");
            fail("A used link must not work twice.");
        } catch (StoreException expected) {
            assertEquals("The reset link is invalid or has expired.", expected.getMessage());
        }
        assertEquals(2, (int) users.login("customer@example.test", "a-brand-new-password").getId());
    }

    @Test
    public void onlyTheHashOfTheTokenIsStored() throws Exception {
        service.requestReset("customer@example.test", "http://x/password-reset");

        assertEquals(1, count("SELECT COUNT(*) FROM password_reset_tokens WHERE user_id=2"));
        assertEquals(0, count("SELECT COUNT(*) FROM password_reset_tokens WHERE token_hash='token-1'"));
        assertEquals(64, count("SELECT CHAR_LENGTH(token_hash) FROM password_reset_tokens"));
        assertTrue(sentBodies.get(0).contains("token=token-1"));
    }

    @Test
    public void anExpiredTokenIsRefusedAndLeavesThePasswordUntouched() throws Exception {
        service.requestReset("customer@example.test", "http://x/password-reset");
        execute("UPDATE password_reset_tokens SET expires_at = DATE_SUB(UTC_TIMESTAMP(), INTERVAL 1 MINUTE)");

        try {
            service.resetPassword("token-1", "a-brand-new-password");
            fail("An expired link must not work.");
        } catch (StoreException expected) {
            assertEquals("The reset link is invalid or has expired.", expected.getMessage());
        }
        assertEquals(0, count("SELECT COUNT(*) FROM password_reset_tokens WHERE used_at IS NOT NULL"));
        assertEquals(2, (int) users.login("customer@example.test", "development-password").getId());
    }

    @Test
    public void aNewRequestInvalidatesTheEarlierLink() throws Exception {
        service.requestReset("customer@example.test", "http://x/password-reset");
        service.requestReset("customer@example.test", "http://x/password-reset");

        try {
            service.resetPassword("token-1", "a-brand-new-password");
            fail("The older link must be replaced.");
        } catch (StoreException expected) {
            assertEquals("The reset link is invalid or has expired.", expected.getMessage());
        }
        service.resetPassword("token-2", "a-brand-new-password");
        assertEquals(2, (int) users.login("customer@example.test", "a-brand-new-password").getId());
    }

    @Test
    public void aTokenNeverResetsAnotherAccount() throws Exception {
        service.requestReset("customer@example.test", "http://x/password-reset");

        service.resetPassword("token-1", "a-brand-new-password");

        assertEquals(1, (int) users.login("admin@example.test", "development-password").getId());
    }

    @Test
    public void unknownTokensAndAccountsChangeNothing() throws Exception {
        service.requestReset("nobody@example.test", "http://x/password-reset");

        assertEquals(0, count("SELECT COUNT(*) FROM password_reset_tokens"));
        assertTrue(sentBodies.isEmpty());
        assertNull(resetData.redeemToken("unknown-hash"));
    }

    @Test
    public void aLegacyHashAccountCannotSignInAndRecoversThroughTheEmailLink() throws Exception {
        execute("UPDATE users SET password='plain-old-password' WHERE id=2");

        try {
            users.login("customer@example.test", "plain-old-password");
            fail("A legacy hash must force a reset.");
        } catch (PasswordResetRequiredException required) {
            assertEquals("customer@example.test", required.getEmail());
        }
        assertEquals("the legacy hash is not silently upgraded", "plain-old-password",
                text("SELECT password FROM users WHERE id=2"));

        service.requestReset("customer@example.test", "http://x/password-reset");
        service.resetPassword("token-1", "a-brand-new-password");

        assertEquals(2, (int) users.login("customer@example.test", "a-brand-new-password").getId());
        assertFalse(text("SELECT password FROM users WHERE id=2").contains("plain-old"));
    }

    @Test
    public void deletingAnAccountRemovesItsTokens() throws Exception {
        execute("INSERT INTO users (id, name, email, password, role_id) VALUES (90, 'Temp', 'temp@example.test', 'x', 2)");
        service.requestReset("temp@example.test", "http://x/password-reset");
        assertEquals(1, count("SELECT COUNT(*) FROM password_reset_tokens WHERE user_id=90"));

        execute("DELETE FROM users WHERE id=90");

        assertEquals(0, count("SELECT COUNT(*) FROM password_reset_tokens WHERE user_id=90"));
    }

    private void execute(String sql) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private int count(String sql) throws Exception {
        return Integer.parseInt(text(sql));
    }

    private String text(String sql) throws Exception {
        try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery(sql)) {
            rows.next();
            return rows.getString(1);
        }
    }
}
