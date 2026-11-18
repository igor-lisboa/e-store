package br.uff.store;

import br.uff.store.core.dtos.CustomerProductDTO;
import br.uff.store.core.dtos.ProductFilterDTO;
import br.uff.store.core.dtos.ProductReviewInputDTO;
import br.uff.store.core.dtos.ProductReviewListDTO;
import br.uff.store.core.exceptions.StoreException;
import org.junit.Test;
import static org.junit.Assert.*;

public class LegacyStoreIntegrationReviewsTest extends StoreIntegrationFixture {
    @Test
    public void reviewRetainsItsAuthorRatingAndText() throws Exception {
        reviews.reviewProduct(new ProductReviewInputDTO(2, 1, 4, "Useful console", "Customer review"));
        assertEquals(1, reviews.listProductReviews(1).size());
        ProductReviewListDTO saved = reviews.listProductReviews(1).get(0);
        assertEquals("Development Customer", saved.getReviewer());
        assertEquals("4", saved.getRating());
        assertEquals("Customer review", saved.getTitle());
        assertEquals("Useful console", saved.getDescription());
    }

    @Test
    public void duplicateReviewIsRejectedWithoutChangingOriginal() throws Exception {
        reviews.reviewProduct(new ProductReviewInputDTO(2, 1, 4, "Original description", "Original title"));
        StoreException failure = assertThrows(StoreException.class, () -> reviews.reviewProduct(
                new ProductReviewInputDTO(2, 1, 1, "Replacement", "Replacement")));
        assertEquals("This user has already reviewed this product.", failure.getMessage());
        assertEquals(1, reviews.listProductReviews(1).size());
        assertEquals("Original title", reviews.listProductReviews(1).get(0).getTitle());
        assertEquals("4", reviews.listProductReviews(1).get(0).getRating());
    }

    @Test
    public void customerViewAggregatesRatingsAndKeepsPersonalRatingSeparate() throws Exception {
        CustomerProductDTO unrated = products.getCustomerProduct(1, 2);
        assertEquals(0.0, unrated.getAverageRating(), 0.001);
        assertEquals(Integer.valueOf(0), unrated.getRatingCount());
        reviews.reviewProduct(new ProductReviewInputDTO(1, 1, 5, "Admin opinion", "Five stars"));
        reviews.reviewProduct(new ProductReviewInputDTO(2, 1, 3, "Customer opinion", "Three stars"));
        CustomerProductDTO rated = products.getCustomerProduct(1, 2);
        assertEquals(4.0, rated.getAverageRating(), 0.001);
        assertEquals(Integer.valueOf(2), rated.getRatingCount());
        assertEquals(Integer.valueOf(3), rated.getUserRating());
        assertEquals(Integer.valueOf(1), rated.getRatingCount5());
        assertEquals(Integer.valueOf(1), rated.getRatingCount3());
    }

    @Test
    public void favoritesToggleAndFilterRemainSpecificToTheirOwner() throws Exception {
        assertFalse(products.isFavorite(1, 2));
        products.toggleFavorite(1, 2);
        assertTrue(products.isFavorite(1, 2));
        assertFalse(products.isFavorite(1, 1));
        assertTrue(products.getCustomerProduct(1, 2).getFavorite());
        ProductFilterDTO filter = new ProductFilterDTO();
        filter.setPageSize(10);
        filter.setPageNumber(1);
        filter.setFavoritesOnly(true);
        filter.setUserId(2);
        assertEquals(1, products.listStorefrontProducts(filter).getItems().size());
        assertEquals(Integer.valueOf(1), products.listStorefrontProducts(filter).getItems().get(0).getId());
        products.toggleFavorite(1, 2);
        assertFalse(products.isFavorite(1, 2));
        assertTrue(products.listStorefrontProducts(filter).getItems().isEmpty());
    }
}
