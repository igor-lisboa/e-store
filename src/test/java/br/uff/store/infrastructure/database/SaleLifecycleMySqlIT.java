package br.uff.store.infrastructure.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import br.uff.store.core.dtos.DeliveryResultDTO;
import br.uff.store.core.dtos.PaymentResultDTO;
import br.uff.store.core.dtos.SaleDTO;
import br.uff.store.core.dtos.SaleLineDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.infrastructure.data.SaleData;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import org.flywaydb.core.Flyway;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Order line snapshots (decision 8A) and cancellation (decision 7B) against the migrated MySQL schema. */
public class SaleLifecycleMySqlIT {
    private static final String ADDRESS = "INSERT INTO address VALUES (1, 'Home', 2, 12345, 'Main Street', 'City', 'State', 'Country')";
    private static final String CART = "INSERT INTO carts (id, user_id, ip, created_at, last_activity_at) VALUES (1, 2, '127.0.0.1', NOW(), NOW())";

    private static final PaymentResultDTO DEMO_PAYMENT =
            new PaymentResultDTO("demo", null, PaymentResultDTO.NOT_CHARGED, null);
    private static final DeliveryResultDTO DEMO_DELIVERY =
            new DeliveryResultDTO("demo", "NONE", null, DeliveryResultDTO.NOT_ARRANGED, null);

    private Connection connection;
    private SaleData sales;

    @Before
    public void setUp() throws Exception {
        MySqlTestDatabase.reset();
        openConnections();
        execute(ADDRESS);
        execute(CART);
        execute("INSERT INTO carts_products (cart_id, product_id, quantity) VALUES (1, 1, 2), (1, 2, 1)");
        sales.recordSale(1, 1, DEMO_PAYMENT, DEMO_DELIVERY);
    }

    @After
    public void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void checkoutStoresNameUnitPriceAndQuantityForEveryLine() throws Exception {
        List<SaleLineDTO> lines = sales.listSaleLines(saleId());

        assertEquals(2, lines.size());
        assertEquals("PlayStation Console", lines.get(0).getProductName());
        assertEquals(new BigDecimal("499.00"), lines.get(0).getUnitPrice());
        assertEquals(Integer.valueOf(2), lines.get(0).getQuantity());
        assertEquals(new BigDecimal("998.00"), lines.get(0).getLineTotal());
        assertEquals("Xbox Console", lines.get(1).getProductName());
    }

    @Test
    public void laterPriceAndNameChangesDoNotRewriteOrderHistory() throws Exception {
        execute("UPDATE products SET name='Renamed', price=1.00 WHERE id=1");

        SaleLineDTO line = sales.listSaleLines(saleId()).get(0);

        assertEquals("PlayStation Console", line.getProductName());
        assertEquals(new BigDecimal("499.00"), line.getUnitPrice());
    }

    @Test
    public void deletingAProductKeepsItsPurchasedLinesWithoutALink() throws Exception {
        execute("DELETE FROM products WHERE id=1");

        List<SaleLineDTO> lines = sales.listSaleLines(saleId());

        assertEquals(2, lines.size());
        assertEquals("PlayStation Console", lines.get(0).getProductName());
        assertEquals(Integer.valueOf(2), lines.get(0).getQuantity());
        assertNull(lines.get(0).getImage());
    }

    @Test
    public void cancellingRestoresPurchasedStockAndMarksTheOrder() throws Exception {
        assertEquals(98, count("SELECT quantity FROM products WHERE id=1"));

        sales.cancelSale(saleId(), 2);

        assertEquals(100, count("SELECT quantity FROM products WHERE id=1"));
        assertEquals(100, count("SELECT quantity FROM products WHERE id=2"));
        assertEquals("CANCELLED", text("SELECT status FROM sales WHERE id=" + saleId()));
        assertNotNull(text("SELECT cancelled_at FROM sales WHERE id=" + saleId()));
    }

    @Test
    public void anotherCustomerCannotCancelAndNothingChanges() throws Exception {
        assertCancelRejected(saleId(), 1);

        assertEquals("PLACED", text("SELECT status FROM sales WHERE id=" + saleId()));
        assertEquals(98, count("SELECT quantity FROM products WHERE id=1"));
    }

    @Test
    public void cancellingTwiceDoesNotRestoreStockTwice() throws Exception {
        sales.cancelSale(saleId(), 2);

        assertCancelRejected(saleId(), 2);

        assertEquals(100, count("SELECT quantity FROM products WHERE id=1"));
    }

    @Test
    public void shippedOrdersCannotBeCancelled() throws Exception {
        execute("UPDATE sales SET status='SHIPPED' WHERE id=" + saleId());

        assertCancelRejected(saleId(), 2);

        assertEquals("SHIPPED", text("SELECT status FROM sales WHERE id=" + saleId()));
        assertEquals(98, count("SELECT quantity FROM products WHERE id=1"));
    }

    @Test
    public void missingOrdersAreRejected() {
        assertCancelRejected(9999, 2);
    }

    @Test
    public void cancellingAfterAProductWasDeletedStillCancelsAndRestoresTheRest() throws Exception {
        execute("DELETE FROM products WHERE id=1");

        sales.cancelSale(saleId(), 2);

        assertEquals("CANCELLED", text("SELECT status FROM sales WHERE id=" + saleId()));
        assertEquals(100, count("SELECT quantity FROM products WHERE id=2"));
    }

    @Test
    public void historyBeforeSnapshotsIsBackfilledWithCurrentProductData() throws Exception {
        connection.close();
        Flyway remaining = MySqlTestDatabase.resetTo("8");
        openConnections();
        execute(ADDRESS);
        execute(CART);
        execute("INSERT INTO carts_products (cart_id, product_id, quantity) VALUES (1, 3, 4)");
        execute("INSERT INTO sales (cart_id, total_price, created_at, address_id, user_id) VALUES (1, 796.00, NOW(), 1, 2)");

        remaining.migrate();

        List<SaleLineDTO> lines = sales.listSaleLines(saleId());
        assertEquals(1, lines.size());
        assertEquals("Wii Console", lines.get(0).getProductName());
        assertEquals(Integer.valueOf(4), lines.get(0).getQuantity());
        assertEquals("PLACED", text("SELECT status FROM sales WHERE id=" + saleId()));
    }

    @Test
    public void checkoutRecordsProviderNeutralPaymentAndDeliveryWithTheOrder() throws Exception {
        execute("INSERT INTO carts (id, user_id, ip, created_at, last_activity_at) VALUES (2, 2, '127.0.0.1', NOW(), NOW())");
        execute("INSERT INTO carts_products (cart_id, product_id, quantity) VALUES (2, 3, 1)");
        PaymentResultDTO payment = new PaymentResultDTO("example-pay", "pay_123", PaymentResultDTO.AUTHORIZED, "s3://raw/pay_123");
        DeliveryResultDTO delivery = new DeliveryResultDTO("example-ship", "EXPRESS", "TRK999", DeliveryResultDTO.PENDING, "s3://raw/ship_9");

        sales.recordSale(2, 1, payment, delivery);

        int orderId = count("SELECT id FROM sales WHERE cart_id=2");
        assertEquals("example-pay", text("SELECT provider FROM payments WHERE sale_id=" + orderId));
        assertEquals("pay_123", text("SELECT external_reference FROM payments WHERE sale_id=" + orderId));
        assertEquals("AUTHORIZED", text("SELECT status FROM payments WHERE sale_id=" + orderId));
        assertEquals("199.00", text("SELECT amount FROM payments WHERE sale_id=" + orderId));
        assertEquals("s3://raw/pay_123", text("SELECT raw_response_ref FROM payments WHERE sale_id=" + orderId));
        assertEquals("EXPRESS", text("SELECT method FROM deliveries WHERE sale_id=" + orderId));
        assertEquals("TRK999", text("SELECT tracking_code FROM deliveries WHERE sale_id=" + orderId));
        assertEquals("PENDING", text("SELECT status FROM deliveries WHERE sale_id=" + orderId));
    }

    @Test
    public void deliveryAddressIsASnapshotThatLaterAddressEditsDoNotChange() throws Exception {
        execute("UPDATE address SET address='Changed Avenue', city='Elsewhere' WHERE id=1");

        SaleDTO sale = sales.listUserSales(2).get(0);

        assertEquals("Main Street", sale.getAddress().getStreetAddress());
        assertEquals("City", sale.getAddress().getCity());
        assertEquals(Integer.valueOf(12345), sale.getAddress().getPostalCode());
        assertEquals("demo", sale.getPaymentProvider());
        assertEquals("NOT_CHARGED", sale.getPaymentStatus());
        assertEquals("NONE", sale.getDeliveryMethod());
        assertEquals("PLACED", sale.getStatus());
    }

    @Test
    public void failedCheckoutLeavesNoPaymentOrDeliveryRecords() throws Exception {
        execute("INSERT INTO carts (id, user_id, ip, created_at, last_activity_at) VALUES (2, 2, '127.0.0.1', NOW(), NOW())");
        execute("INSERT INTO carts_products (cart_id, product_id, quantity) VALUES (2, 3, 1000)");

        try {
            sales.recordSale(2, 1, DEMO_PAYMENT, DEMO_DELIVERY);
            fail("Checkout beyond stock must fail.");
        } catch (StoreException expected) {
            assertEquals(1, count("SELECT COUNT(*) FROM payments"));
            assertEquals(1, count("SELECT COUNT(*) FROM deliveries"));
            assertEquals(1, count("SELECT COUNT(*) FROM sales"));
        }
    }

    @Test
    public void unknownPaymentStatusIsRejectedByTheDatabase() throws Exception {
        execute("INSERT INTO carts (id, user_id, ip, created_at, last_activity_at) VALUES (2, 2, '127.0.0.1', NOW(), NOW())");
        execute("INSERT INTO carts_products (cart_id, product_id, quantity) VALUES (2, 3, 1)");

        try {
            sales.recordSale(2, 1, new PaymentResultDTO("demo", null, "PAID_MAYBE", null), DEMO_DELIVERY);
            fail("Only known payment statuses may be stored.");
        } catch (StoreException expected) {
            assertEquals(1, count("SELECT COUNT(*) FROM sales"));
            assertEquals(100, count("SELECT quantity FROM products WHERE id=3"));
        }
    }

    @Test
    public void legacyTwoArgumentCheckoutStillWorksWithDemoDefaults() throws Exception {
        execute("INSERT INTO carts (id, user_id, ip, created_at, last_activity_at) VALUES (2, 2, '127.0.0.1', NOW(), NOW())");
        execute("INSERT INTO carts_products (cart_id, product_id, quantity) VALUES (2, 3, 1)");

        execute("CALL checkout_cart(2, 1)");

        int orderId = count("SELECT id FROM sales WHERE cart_id=2");
        assertEquals("demo", text("SELECT provider FROM payments WHERE sale_id=" + orderId));
        assertEquals("NOT_ARRANGED", text("SELECT status FROM deliveries WHERE sale_id=" + orderId));
    }

    @Test
    public void ordersBeforeProviderRecordsAreBackfilledAsDemoWithAnAddressSnapshot() throws Exception {
        connection.close();
        Flyway remaining = MySqlTestDatabase.resetTo("10");
        openConnections();
        execute(ADDRESS);
        execute(CART);
        execute("INSERT INTO carts_products (cart_id, product_id, quantity) VALUES (1, 3, 2)");
        execute("INSERT INTO sales (cart_id, total_price, created_at, address_id, user_id) VALUES (1, 398.00, NOW(), 1, 2)");

        remaining.migrate();

        SaleDTO sale = sales.listUserSales(2).get(0);
        assertEquals("demo", sale.getPaymentProvider());
        assertEquals("NOT_CHARGED", sale.getPaymentStatus());
        assertEquals("Main Street", sale.getAddress().getStreetAddress());
        assertEquals("398.00", text("SELECT amount FROM payments WHERE sale_id=" + sale.getId()));
    }

    private void openConnections() throws Exception {
        String url = System.getenv("TEST_MYSQL_URL");
        String user = System.getenv("TEST_MYSQL_USER");
        String password = System.getenv("TEST_MYSQL_PASSWORD");
        connection = DriverManager.getConnection(url, user, password);
        sales = new SaleData(new MySQLDAO(() -> DriverManager.getConnection(url, user, password)));
    }

    private void assertCancelRejected(int saleId, int userId) {
        try {
            sales.cancelSale(saleId, userId);
            fail("The cancellation must be rejected.");
        } catch (StoreException expected) {
            assertNotNull(expected.getCause());
        }
    }

    private int saleId() throws Exception {
        return count("SELECT id FROM sales ORDER BY id LIMIT 1");
    }

    private void execute(String sql) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private int count(String sql) throws Exception {
        return Integer.parseInt(text(sql));
    }

    private String text(String sql) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            assertTrue(rows.next());
            return rows.getString(1);
        }
    }
}
