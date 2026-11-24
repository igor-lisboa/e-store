package br.uff.store.infrastructure.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import br.uff.store.core.exceptions.StoreException;
import br.uff.store.infrastructure.data.CartData;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Runs cart merge and expiry SQL against the migrated MySQL schema in the disposable test database. */
public class CartActivityMySqlIT {
    private Connection connection;
    private CartData carts;

    @Before
    public void setUp() throws Exception {
        MySqlTestDatabase.reset();
        String url = System.getenv("TEST_MYSQL_URL");
        String user = System.getenv("TEST_MYSQL_USER");
        String password = System.getenv("TEST_MYSQL_PASSWORD");
        connection = DriverManager.getConnection(url, user, password);
        carts = new CartData(new MySQLDAO(() -> DriverManager.getConnection(url, user, password)));
        execute("UPDATE products SET quantity=5 WHERE id=1");
        execute("UPDATE products SET quantity=10 WHERE id=2");
        execute("UPDATE products SET quantity=0 WHERE id=3");
        execute("INSERT INTO carts (id, user_id, ip, created_at, last_activity_at) VALUES "
                + "(1, NULL, '10.0.0.1', '2026-01-01 10:00:00', '2026-01-01 10:00:00'), "
                + "(2, 2, '10.0.0.2', '2026-01-01 10:00:00', '2026-01-01 10:00:00')");
    }

    @After
    public void tearDown() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    public void mergeCapsCombinedQuantityAtCurrentStock() throws Exception {
        execute("INSERT INTO carts_products (cart_id, product_id, quantity) VALUES (2, 1, 3), (1, 1, 4)");

        carts.mergeCart(1, 2);

        assertEquals(5, quantityOf(2, 1));
        assertEquals(0, count("SELECT COUNT(*) FROM carts_products WHERE cart_id=1"));
    }

    @Test
    public void mergeMovesMissingProductsAndEmptiesTheSourceCart() throws Exception {
        execute("INSERT INTO carts_products (cart_id, product_id, quantity) VALUES (1, 2, 2)");

        carts.mergeCart(1, 2);

        assertEquals(2, quantityOf(2, 2));
        assertEquals(0, count("SELECT COUNT(*) FROM carts_products WHERE cart_id=1"));
    }

    @Test
    public void mergeDropsProductsWithoutStockInsteadOfCreatingZeroQuantityRows() throws Exception {
        execute("INSERT INTO carts_products (cart_id, product_id, quantity) VALUES (1, 3, 1)");

        carts.mergeCart(1, 2);

        assertEquals(0, count("SELECT COUNT(*) FROM carts_products WHERE cart_id=2 AND product_id=3"));
        assertEquals(0, count("SELECT COUNT(*) FROM carts_products WHERE cart_id=1"));
    }

    @Test
    public void failedMergeRollsBackAndKeepsTheSourceCartItems() throws Exception {
        execute("INSERT INTO carts_products (cart_id, product_id, quantity) VALUES (1, 2, 2)");

        try {
            carts.mergeCart(1, 999);
            fail("Merging into a missing cart must fail.");
        } catch (StoreException expected) {
            assertEquals(1, count("SELECT COUNT(*) FROM carts_products WHERE cart_id=1"));
            assertEquals(2, quantityOf(1, 2));
        }
    }

    @Test
    public void expiryUsesActivityTimeInclusivelyAtTheBoundary() throws Exception {
        assertTrue(carts.isCartActiveSince(1, "2026-01-01 10:00:00"));
        assertFalse(carts.isCartActiveSince(1, "2026-01-01 10:00:01"));
    }

    @Test
    public void touchingACartMovesItsActivityForward() throws Exception {
        carts.touchCart(1, "2026-02-01 09:00:00");

        assertTrue(carts.isCartActiveSince(1, "2026-01-31 09:00:00"));
        assertFalse(carts.isCartActiveSince(1, "2026-02-01 09:00:01"));
    }

    private int quantityOf(int cartId, int productId) throws Exception {
        return count("SELECT quantity FROM carts_products WHERE cart_id=" + cartId + " AND product_id=" + productId);
    }

    private void execute(String sql) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private int count(String sql) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            assertTrue(rows.next());
            return rows.getInt(1);
        }
    }
}
