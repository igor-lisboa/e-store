package br.uff.store.infrastructure.database;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import br.uff.store.core.exceptions.ResourceNotFoundException;
import br.uff.store.infrastructure.data.UserData;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UserRepositoryIntegrationTest {
    private Connection fixture;
    private UserData users;

    @Before
    public void setUp() throws Exception {
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DATABASE_TO_UPPER=FALSE";
        fixture = DriverManager.getConnection(url);
        try (Statement statement = fixture.createStatement()) {
            statement.execute("CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(255), email VARCHAR(255), "
                    + "password VARCHAR(255), role_id INT)");
            statement.execute("INSERT INTO users VALUES (1, 'Customer', 'customer@example.test', 'encoded', 2)");
        }
        users = new UserData(new MySQLDAO(() -> DriverManager.getConnection(url)));
    }

    @After
    public void tearDown() throws Exception {
        fixture.close();
    }

    @Test
    public void duplicateEmailChecksTreatCreationAndEditingDifferently() throws Exception {
        assertTrue(users.isEmailInUse("customer@example.test", "null"));
        assertTrue(users.isEmailInUse("customer@example.test", null));
        assertFalse(users.isEmailInUse("customer@example.test", "1"));
        assertTrue(users.isEmailInUse("customer@example.test", "2"));
        assertFalse(users.isEmailInUse("new@example.test", "null"));
    }

    @Test
    public void lookupReturnsTheRequestedAccountRatherThanAnUnrelatedRow() throws Exception {
        assertEquals("customer@example.test", users.findUserById(1).getEmail());
        try {
            users.findUserById(99);
            fail("An absent account must produce a not-found error.");
        } catch (ResourceNotFoundException expected) {
            assertEquals("User was not found.", expected.getMessage());
        }
    }
}
