package br.uff.store.infrastructure.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import br.uff.store.core.dtos.CartDTO;
import br.uff.store.core.dtos.CustomerProductDTO;
import br.uff.store.core.dtos.ProductListDTO;
import br.uff.store.core.dtos.SaleDTO;
import br.uff.store.core.dtos.SaleLineDTO;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

/** Rows arrive from JDBC as strings or nulls; the mapper must turn them into correctly typed DTOs. */
public class RowMapperTest {
    private static Map<String, Object> row(Object... pairs) {
        Map<String, Object> row = new HashMap<>();
        for (int index = 0; index < pairs.length; index += 2) {
            row.put((String) pairs[index], pairs[index + 1]);
        }
        return row;
    }

    private static Map<String, Object> customerProductRow(String ratingCount, String ratingSum, String... starCounts) {
        return row("id", "7", "name", "Console", "price", "99.90", "description", "Fast", "image", "img.png",
                "category", "Xbox", "quantity", "5", "favorite", "1", "userRating", null, "ratingCount", ratingCount,
                "ratingSum", ratingSum, "ratingCount1", starCounts[0], "ratingCount2", starCounts[1],
                "ratingCount3", starCounts[2], "ratingCount4", starCounts[3], "ratingCount5", starCounts[4]);
    }

    @Test
    public void availabilityFlagUnderstandsNumericAndBooleanTruthAndTreatsAnythingElseAsUnavailable() {
        Map<String, Object> base = row("id", "1", "name", "A", "price", "1.00", "image", "i", "category", "c");
        for (Object available : new Object[] {1L, "1", Boolean.TRUE, "true"}) {
            base.put("available", available);
            assertTrue(String.valueOf(available), RowMapper.productListItem(base).isAvailable());
        }
        for (Object unavailable : new Object[] {0L, "0", Boolean.FALSE, "false", null}) {
            base.put("available", unavailable);
            assertFalse(String.valueOf(unavailable), RowMapper.productListItem(base).isAvailable());
        }
    }

    @Test
    public void listItemKeepsExactDecimalPrices() {
        ProductListDTO item = RowMapper.productListItem(row("id", "3", "name", "Pad", "price", "19.90", "image", "x",
                "category", "Wii", "available", "1"));

        assertEquals(Integer.valueOf(3), item.getId());
        assertEquals(new BigDecimal("19.90"), item.getPrice());
    }

    @Test
    public void cartOwnerIsOptionalAndTimestampsAreReadAsUtc() {
        CartDTO anonymous = RowMapper.cart(row("id", "4", "ip", "10.0.0.1", "userId", null, "createdAt", "2026-03-01 10:00:00"));
        CartDTO owned = RowMapper.cart(row("id", "5", "ip", "10.0.0.2", "userId", "22", "createdAt", "2026-03-01 10:00:00"));

        assertNull(anonymous.getUserId());
        assertEquals(Integer.valueOf(22), owned.getUserId());
        assertEquals(new Date(1772359200000L), anonymous.getCreatedAt());
    }

    @Test
    public void unreadableTimestampsBecomeNullInsteadOfFailingTheWholeRow() {
        CartDTO cart = RowMapper.cart(row("id", "4", "ip", "x", "userId", null, "createdAt", "not-a-date"));

        assertNull(cart.getCreatedAt());
    }

    @Test
    public void aMissingRequiredColumnIsAnErrorNotASilentNull() {
        try {
            RowMapper.user(row("name", "Ana", "email", "a@x", "password", "p", "roleId", "2"));
            fail("A row without an id must not map.");
        } catch (NumberFormatException expected) {
            // Expected: the id column is required.
        }
    }

    @Test
    public void customerProductComputesAverageAndPercentagesFromTheStoredCounts() {
        CustomerProductDTO product = RowMapper.customerProduct(customerProductRow("4", "16", "0", "0", "1", "1", "2"));

        assertEquals(4.0, product.getAverageRating(), 0.0001);
        assertEquals(25.0, product.getRatingBar3(), 0.0001);
        assertEquals(50.0, product.getRatingBar5(), 0.0001);
        assertEquals(0.0, product.getRatingBar1(), 0.0001);
        assertTrue(product.getFavorite());
        assertNull(product.getUserRating());
    }

    @Test
    public void customerProductWithoutRatingsHasZeroSummaryInsteadOfDividingByZero() {
        CustomerProductDTO product = RowMapper.customerProduct(customerProductRow("0", "0", "0", "0", "0", "0", "0"));

        assertEquals(0.0, product.getAverageRating(), 0.0);
        assertEquals(0.0, product.getRatingBar5(), 0.0);
    }

    @Test
    public void customerProductReadsTheUsersOwnRatingAndNotFavoriteState() {
        Map<String, Object> source = customerProductRow("1", "5", "0", "0", "0", "0", "1");
        source.put("userRating", "5");
        source.put("favorite", "0");

        CustomerProductDTO product = RowMapper.customerProduct(source);

        assertEquals(Integer.valueOf(5), product.getUserRating());
        assertFalse(product.getFavorite());
    }

    @Test
    public void saleCarriesStatusPaymentAndAnAddressSnapshotOnlyWhenDeliveryExists() {
        Map<String, Object> source = row("id", "9", "cartId", "4", "totalPrice", "398.00", "createdAt", "2026-03-01 10:00:00",
                "addressId", "1", "userId", "2", "status", "CANCELLED", "cancelledAt", "2026-03-02 08:00:00",
                "paymentProvider", "demo", "paymentStatus", "NOT_CHARGED", "deliveryMethod", "NONE", "trackingCode", null,
                "deliveryStreet", "Main Street", "deliveryCity", "City", "deliveryState", "State",
                "deliveryPostalCode", "12345", "deliveryCountry", "Country");

        SaleDTO sale = RowMapper.sale(source);

        assertEquals("CANCELLED", sale.getStatus());
        assertFalse(sale.isCancellable());
        assertEquals(new Date(1772438400000L), sale.getCancelledAt());
        assertEquals("demo", sale.getPaymentProvider());
        assertNull(sale.getTrackingCode());
        assertEquals("Main Street", sale.getAddress().getStreetAddress());
        assertEquals(Integer.valueOf(12345), sale.getAddress().getPostalCode());

        source.put("deliveryStreet", null);
        assertNull(RowMapper.sale(source).getAddress());
    }

    @Test
    public void placedSalesAreCancellable() {
        SaleDTO sale = RowMapper.sale(row("id", "9", "cartId", "4", "totalPrice", "1.00", "createdAt", "2026-03-01 10:00:00",
                "addressId", "1", "userId", "2", "status", "PLACED", "cancelledAt", null));

        assertTrue(sale.isCancellable());
        assertNull(sale.getCancelledAt());
    }

    @Test
    public void saleLineAllowsADeletedProductWithoutLinkOrImage() {
        SaleLineDTO line = RowMapper.saleLine(row("id", "1", "productId", null, "productName", "Gone", "image", null,
                "unitPrice", "10.50", "quantity", "3"));

        assertNull(line.getProductId());
        assertNull(line.getImage());
        assertEquals(new BigDecimal("31.50"), line.getLineTotal());
    }
}
