package br.uff.store.infrastructure.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import org.flywaydb.core.Flyway;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Destructive fixtures are confined to the explicitly named disposable test database. */
public class MySqlMigrationIT {
    private Flyway flyway;
    private Connection connection;

    @Before
    public void setUp() throws Exception {
        flyway = MySqlTestDatabase.reset();
        String url = System.getenv("TEST_MYSQL_URL");
        String user = System.getenv("TEST_MYSQL_USER");
        String password = System.getenv("TEST_MYSQL_PASSWORD");
        connection = DriverManager.getConnection(url, user, password);
    }

    @After
    public void tearDown() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    public void migrationsValidateAndASecondRunPreservesSeededData() throws Exception {
        flyway.validate();
        assertEquals(0, flyway.migrate().migrationsExecuted);
        assertEquals(2, count("SELECT COUNT(*) FROM roles"));
        assertEquals(12, count("SELECT COUNT(*) FROM category"));
        assertEquals(3, count("SELECT COUNT(*) FROM products"));
        assertEquals(9, count("SELECT COUNT(*) FROM vw_category"));
    }

    @Test
    public void stockFailureRollsBackEveryEarlierCheckoutWrite() throws Exception {
        execute("INSERT INTO address VALUES (1, 'Home', 2, 12345, 'Main Street', 'City', 'State', 'Country')");
        execute("INSERT INTO carts (id, user_id, ip, created_at, last_activity_at) VALUES (1, 2, '127.0.0.1', NOW(), NOW())");
        execute("UPDATE products SET quantity=0 WHERE id=2");
        execute("INSERT INTO carts_products VALUES (1, 1, 1, 2), (2, 1, 2, 1)");
        try {
            execute("CALL checkout_cart(1, 1)");
            fail("Checkout with insufficient stock must fail.");
        } catch (SQLException expected) {
            assertEquals(100, count("SELECT quantity FROM products WHERE id=1"));
            assertEquals(0, count("SELECT COUNT(*) FROM sales"));
        }
    }

    @Test
    public void successfulCheckoutStoresDecimalTotalsAndCannotBeRepeated() throws Exception {
        execute("INSERT INTO address VALUES (1, 'Home', 2, 12345, 'Main Street', 'City', 'State', 'Country')");
        execute("INSERT INTO carts (id, user_id, ip, created_at, last_activity_at) VALUES (1, 2, '127.0.0.1', NOW(), NOW())");
        execute("UPDATE products SET price=0.10 WHERE id=1");
        execute("INSERT INTO carts_products VALUES (1, 1, 1, 3)");
        execute("CALL checkout_cart(1, 1)");
        try (Statement query = connection.createStatement(); ResultSet result = query.executeQuery("SELECT total_price FROM sales")) {
            result.next();
            assertEquals(new java.math.BigDecimal("0.30"), result.getBigDecimal(1));
        }
        try {
            execute("CALL checkout_cart(1, 1)");
            fail("A repeated checkout must not create a second sale.");
        } catch (SQLException expected) {
            assertEquals(1, count("SELECT COUNT(*) FROM sales"));
            assertEquals(97, count("SELECT quantity FROM products WHERE id=1"));
        }
    }

    @Test
    public void anotherAccountsAddressCannotBeUsedForCheckout() throws Exception {
        execute("INSERT INTO address VALUES (1, 'Admin Home', 1, 12345, 'Main Street', 'City', 'State', 'Country')");
        execute("INSERT INTO carts (id, user_id, ip, created_at, last_activity_at) VALUES (1, 2, '127.0.0.1', NOW(), NOW())");
        execute("INSERT INTO carts_products VALUES (1, 1, 1, 2)");
        try {
            execute("CALL checkout_cart(1, 1)");
            fail("The procedure must enforce address ownership as well as the service.");
        } catch (SQLException expected) {
            assertEquals(0, count("SELECT COUNT(*) FROM sales"));
            assertEquals(100, count("SELECT quantity FROM products WHERE id=1"));
        }
    }

    @Test
    public void competingCartsCannotOversellTheSameProduct() throws Exception {
        execute("INSERT INTO address VALUES (1, 'Home', 2, 12345, 'Main Street', 'City', 'State', 'Country')");
        execute("INSERT INTO carts (id, user_id, ip, created_at, last_activity_at) VALUES (1, 2, '127.0.0.1', NOW(), NOW()), (2, 2, '127.0.0.1', NOW(), NOW())");
        execute("UPDATE products SET quantity=10 WHERE id=1");
        execute("INSERT INTO carts_products VALUES (1, 1, 1, 7), (2, 2, 1, 7)");
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(2);
        java.util.concurrent.CyclicBarrier barrier = new java.util.concurrent.CyclicBarrier(2);
        try {
            java.util.concurrent.Future<Boolean> first = executor.submit(() -> purchase(1, barrier));
            java.util.concurrent.Future<Boolean> second = executor.submit(() -> purchase(2, barrier));
            int succeeded = (first.get(15, java.util.concurrent.TimeUnit.SECONDS) ? 1 : 0)
                    + (second.get(15, java.util.concurrent.TimeUnit.SECONDS) ? 1 : 0);
            assertEquals(1, succeeded);
            assertEquals(1, count("SELECT COUNT(*) FROM sales"));
            assertEquals(3, count("SELECT quantity FROM products WHERE id=1"));
        } finally {
            executor.shutdownNow();
        }
    }

    private boolean purchase(int cartId, java.util.concurrent.CyclicBarrier barrier) throws Exception {
        try (Connection buyer = DriverManager.getConnection(System.getenv("TEST_MYSQL_URL"),
                System.getenv("TEST_MYSQL_USER"), System.getenv("TEST_MYSQL_PASSWORD"));
                java.sql.CallableStatement checkout = buyer.prepareCall("CALL checkout_cart(?, 1)")) {
            checkout.setInt(1, cartId);
            barrier.await(10, java.util.concurrent.TimeUnit.SECONDS);
            try {
                checkout.execute();
                return true;
            } catch (SQLException failure) {
                if (!"45000".equals(failure.getSQLState()) && !"40001".equals(failure.getSQLState())) {
                    throw failure;
                }
                return false;
            }
        }
    }

    private void execute(String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private int count(String sql) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sql)) {
            result.next();
            return result.getInt(1);
        }
    }
}
