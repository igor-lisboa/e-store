package br.uff.store.infrastructure.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import br.uff.store.core.exceptions.StoreException;
import br.uff.store.infrastructure.data.ProductData;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Stock increments must neither overflow the INT column nor silently succeed for a missing product. */
public class ProductStockMySqlIT {
    private Connection connection;
    private ProductData products;

    @Before
    public void setUp() throws Exception {
        MySqlTestDatabase.reset();
        String url = System.getenv("TEST_MYSQL_URL");
        String user = System.getenv("TEST_MYSQL_USER");
        String password = System.getenv("TEST_MYSQL_PASSWORD");
        connection = DriverManager.getConnection(url, user, password);
        products = new ProductData(new MySQLDAO(() -> DriverManager.getConnection(url, user, password)));
    }

    @After
    public void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void incrementAddsToTheStoredQuantity() throws Exception {
        products.addProductStock(1, 25);

        assertEquals(125, quantity(1));
        assertEquals(100, quantity(2));
    }

    @Test
    public void incrementThatWouldOverflowTheStockColumnIsRejectedAndLeavesStockUnchanged() throws Exception {
        execute("UPDATE products SET quantity=2147483000 WHERE id=1");

        try {
            products.addProductStock(1, 1000);
            fail("Overflowing the stock column must be rejected.");
        } catch (StoreException expected) {
            assertEquals(2147483000, quantity(1));
        }
    }

    @Test
    public void incrementReachingExactlyTheMaximumIsAllowed() throws Exception {
        execute("UPDATE products SET quantity=2147482647 WHERE id=1");

        products.addProductStock(1, 1000);

        assertEquals(2147483647, quantity(1));
    }

    @Test
    public void incrementForAMissingProductIsReportedInsteadOfSilentlyIgnored() throws Exception {
        try {
            products.addProductStock(999, 5);
            fail("A missing product must be reported.");
        } catch (StoreException expected) {
            assertEquals(100, quantity(1));
        }
    }

    private long quantity(int productId) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT quantity FROM products WHERE id=" + productId)) {
            assertTrue(rows.next());
            return rows.getLong(1);
        }
    }

    private void execute(String sql) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
