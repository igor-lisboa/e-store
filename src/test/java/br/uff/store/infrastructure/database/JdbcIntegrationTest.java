package br.uff.store.infrastructure.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Real JDBC behavior; these checks do not replace MySQL migration tests. */
public class JdbcIntegrationTest {
    private Connection fixture;
    private MySQLDAO dao;
    private final List<Connection> opened = Collections.synchronizedList(new ArrayList<>());

    @Before
    public void setUp() throws Exception {
        String url = "jdbc:h2:mem:" + UUID.randomUUID();
        fixture = DriverManager.getConnection(url);
        try (java.sql.Statement statement = fixture.createStatement()) {
            statement.execute("CREATE TABLE inventory (id INT PRIMARY KEY, quantity INT CHECK(quantity >= 0))");
            statement.execute("INSERT INTO inventory VALUES (1, 10)");
        }
        dao = new MySQLDAO(() -> {
            Connection connection = DriverManager.getConnection(url);
            opened.add(connection);
            return connection;
        });
    }

    @After
    public void tearDown() throws Exception {
        fixture.close();
        for (Connection connection : opened) {
            assertTrue("Every DAO connection must be closed.", connection.isClosed());
        }
    }

    @Test
    public void transactionCommitsAllCommands() throws Exception {
        dao.executeTransaction(new String[] {
                "UPDATE inventory SET quantity=quantity-? WHERE id=?",
                "INSERT INTO inventory VALUES (?, ?)"}, new Object[][] {{2, 1}, {2, 7}});

        assertEquals(8, quantity(1));
        assertEquals(7, quantity(2));
        assertEquals(1, opened.size());
    }

    @Test
    public void laterFailureRollsBackEarlierWrites() throws Exception {
        try {
            dao.executeTransaction(new String[] {
                    "UPDATE inventory SET quantity=quantity-2 WHERE id=1",
                    "UPDATE inventory SET quantity=-1 WHERE id=1"}, null);
            fail("A constraint failure must abort the transaction.");
        } catch (SQLException expected) {
            assertEquals(10, quantity(1));
        }
    }

    @Test
    public void queryUsesBindingsAndPreservesSqlNulls() throws Exception {
        dao.executeUpdate("INSERT INTO inventory VALUES (?, ?)", new Object[] {2, null});

        assertEquals("10", dao.query("SELECT quantity AS \"stock\" FROM inventory WHERE id=?",
                new Object[] {1}).get(0).get("stock"));
        assertEquals(null, dao.query("SELECT quantity FROM inventory WHERE id=?", new Object[] {2})
                .get(0).values().iterator().next());
    }

    @Test
    public void sharedDaoDoesNotShareConnectionsBetweenRequests() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> first = executor.submit(() -> dao.executeUpdate(
                    "INSERT INTO inventory VALUES (?, ?)", new Object[] {2, 4}));
            Future<Integer> second = executor.submit(() -> dao.executeUpdate(
                    "INSERT INTO inventory VALUES (?, ?)", new Object[] {3, 6}));
            assertEquals(Integer.valueOf(1), first.get());
            assertEquals(Integer.valueOf(1), second.get());
            assertEquals(4, quantity(2));
            assertEquals(6, quantity(3));
            assertEquals(2, opened.size());
        } finally {
            executor.shutdownNow();
        }
    }

    private int quantity(int id) throws SQLException {
        try (java.sql.PreparedStatement query = fixture.prepareStatement("SELECT quantity FROM inventory WHERE id=?")) {
            query.setInt(1, id);
            try (ResultSet result = query.executeQuery()) {
                assertTrue(result.next());
                return result.getInt(1);
            }
        }
    }
}
