package br.uff.store.infrastructure.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import br.uff.store.core.dtos.ProductReviewInputDTO;
import br.uff.store.infrastructure.data.ProductReviewData;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Review edit and delete SQL (decision 10C) against the migrated MySQL schema. */
public class ReviewOwnershipMySqlIT {
    private Connection connection;
    private ProductReviewData reviews;

    @Before
    public void setUp() throws Exception {
        MySqlTestDatabase.reset();
        String url = System.getenv("TEST_MYSQL_URL");
        String user = System.getenv("TEST_MYSQL_USER");
        String password = System.getenv("TEST_MYSQL_PASSWORD");
        connection = DriverManager.getConnection(url, user, password);
        reviews = new ProductReviewData(new MySQLDAO(() -> DriverManager.getConnection(url, user, password)));
        execute("INSERT INTO product_reviews (user_id, product_id, rating, description, title, created_at) VALUES "
                + "(1, 1, 5, 'Admin text', 'Admin title', NOW()), (2, 1, 2, 'Customer text', 'Customer title', NOW())");
    }

    @After
    public void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void updateChangesOnlyTheCallersReview() throws Exception {
        int changed = reviews.updateProductReview(new ProductReviewInputDTO(2, 1, 4, "Edited", "Edited title"));

        assertEquals(1, changed);
        assertEquals("Edited title", text("SELECT title FROM product_reviews WHERE user_id=2 AND product_id=1"));
        assertEquals("4", text("SELECT rating FROM product_reviews WHERE user_id=2 AND product_id=1"));
        assertEquals("Admin title", text("SELECT title FROM product_reviews WHERE user_id=1 AND product_id=1"));
        assertEquals("5", text("SELECT rating FROM product_reviews WHERE user_id=1 AND product_id=1"));
    }

    @Test
    public void savingIdenticalValuesStillCountsAsAnOwnedReviewNotAMissingOne() throws Exception {
        int changed = reviews.updateProductReview(new ProductReviewInputDTO(2, 1, 2, "Customer text", "Customer title"));

        assertEquals(1, changed);
    }

    @Test
    public void updatingWithoutOwningAReviewChangesNothing() throws Exception {
        assertEquals(0, reviews.updateProductReview(new ProductReviewInputDTO(2, 2, 1, "x", "y")));
        assertEquals(2, count("SELECT COUNT(*) FROM product_reviews"));
    }

    @Test
    public void deleteRemovesOnlyTheCallersReview() throws Exception {
        assertEquals(1, reviews.deleteProductReview(2, 1));

        assertEquals(1, count("SELECT COUNT(*) FROM product_reviews"));
        assertEquals(1, count("SELECT COUNT(*) FROM product_reviews WHERE user_id=1 AND product_id=1"));
    }

    @Test
    public void deletingAReviewTheUserDoesNotOwnRemovesNothing() throws Exception {
        assertEquals(0, reviews.deleteProductReview(2, 3));
        assertEquals(2, count("SELECT COUNT(*) FROM product_reviews"));
    }

    @Test
    public void lookupReturnsOnlyTheUsersOwnReviewOrNull() throws Exception {
        ProductReviewInputDTO own = reviews.findUserReview(2, 1);

        assertEquals("Customer title", own.getTitle());
        assertEquals("Customer text", own.getDescription());
        assertEquals(Integer.valueOf(2), own.getRating());
        assertNull(reviews.findUserReview(2, 2));
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
