package br.uff.store.infrastructure.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * A cart edit that races with checkout must never leave the order inconsistent: whatever quantity the
 * checkout locked and read is exactly what the order lines, the total, and the stock deduction reflect.
 */
public class CheckoutRaceMySqlIT {
    private static final int ROUNDS = 12;
    private static final int UNIT_PRICE_CENTS = 49900;

    private Connection connection;

    @Before
    public void setUp() throws Exception {
        MySqlTestDatabase.reset();
        connection = open();
        execute("INSERT INTO address VALUES (1, 'Home', 2, 12345, 'Main Street', 'City', 'State', 'Country')");
    }

    @After
    public void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void concurrentCartEditsNeverMakeTheOrderDisagreeWithItselfOrWithStock() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            for (int round = 1; round <= ROUNDS; round++) {
                prepareCart();
                CyclicBarrier start = new CyclicBarrier(2);
                Future<?> checkout = executor.submit(() -> checkout(1, start));
                Future<?> edits = executor.submit(() -> editCart(start));
                checkout.get(30, TimeUnit.SECONDS);
                edits.get(30, TimeUnit.SECONDS);
                assertOrderIsConsistent();
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private void prepareCart() throws Exception {
        execute("DELETE FROM sale_lines");
        execute("DELETE FROM payments");
        execute("DELETE FROM deliveries");
        execute("DELETE FROM sales");
        execute("DELETE FROM carts_products");
        execute("DELETE FROM carts");
        execute("UPDATE products SET quantity=100 WHERE id=1");
        execute("INSERT INTO carts (id, user_id, ip, created_at, last_activity_at) VALUES (1, 2, '127.0.0.1', NOW(), NOW())");
        execute("INSERT INTO carts_products (cart_id, product_id, quantity) VALUES (1, 1, 1)");
    }

    private Void checkout(int cartId, CyclicBarrier start) throws Exception {
        try (Connection buyer = open(); CallableStatement call = buyer.prepareCall("CALL checkout_cart(?, 1)")) {
            call.setInt(1, cartId);
            start.await(10, TimeUnit.SECONDS);
            try {
                call.execute();
            } catch (SQLException conflict) {
                if (!"40001".equals(conflict.getSQLState())) {
                    throw conflict;
                }
                // A deadlock victim is rolled back cleanly; the order is then placed without competition.
                call.execute();
            }
        }
        return null;
    }

    private Void editCart(CyclicBarrier start) throws Exception {
        try (Connection editor = open(); Statement statement = editor.createStatement()) {
            start.await(10, TimeUnit.SECONDS);
            for (int edit = 0; edit < 15; edit++) {
                try {
                    statement.executeUpdate("UPDATE carts_products SET quantity = quantity + 1 WHERE cart_id=1 AND product_id=1");
                } catch (SQLException lockConflict) {
                    if (!"40001".equals(lockConflict.getSQLState())) {
                        throw lockConflict;
                    }
                }
            }
        }
        return null;
    }

    private void assertOrderIsConsistent() throws Exception {
        assertEquals(1, count("SELECT COUNT(*) FROM sales"));
        int soldQuantity = count("SELECT quantity FROM sale_lines");
        assertEquals("stock deduction must equal the purchased line quantity", 100 - soldQuantity,
                count("SELECT quantity FROM products WHERE id=1"));
        assertEquals("order total must equal the purchased line quantity at the unit price",
                soldQuantity * UNIT_PRICE_CENTS, count("SELECT ROUND(total_price * 100) FROM sales"));
        assertEquals(count("SELECT ROUND(total_price * 100) FROM sales"),
                count("SELECT ROUND(amount * 100) FROM payments"));
    }

    private Connection open() throws SQLException {
        return DriverManager.getConnection(System.getenv("TEST_MYSQL_URL"), System.getenv("TEST_MYSQL_USER"),
                System.getenv("TEST_MYSQL_PASSWORD"));
    }

    private void execute(String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private int count(String sql) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery(sql)) {
            assertTrue(rows.next());
            return rows.getInt(1);
        }
    }
}
