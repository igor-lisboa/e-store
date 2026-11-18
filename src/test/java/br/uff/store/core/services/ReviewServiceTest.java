package br.uff.store.core.services;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.uff.store.core.dtos.ProductReviewInputDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IProductReviewData;
import org.junit.Before;
import org.junit.Test;

public class ReviewServiceTest {
    private IProductReviewData reviewData;
    private ReviewService service;

    @Before
    public void setUp() {
        reviewData = mock(IProductReviewData.class);
        service = new ReviewService(reviewData);
    }

    @Test
    public void acceptsRatingsFromOneToFive() throws Exception {
        ProductReviewInputDTO review = new ProductReviewInputDTO(4, 7, 5, "Review", "Title");
        when(reviewData.jaFoiAvaliado(4, 7)).thenReturn(false);

        service.avaliaProduct(review);

        verify(reviewData).insereReviewDoProduct(review);
    }

    @Test
    public void rejectsRatingsOutsideTheSupportedRange() throws Exception {
        assertInvalid(new ProductReviewInputDTO(4, 7, 0, "Review", "Title"));
        assertInvalid(new ProductReviewInputDTO(4, 7, 6, "Review", "Title"));
    }

    @Test
    public void rejectsMissingUsersAndBlankTitles() throws Exception {
        assertInvalid(new ProductReviewInputDTO(null, 7, 4, "Review", "Title"));
        assertInvalid(new ProductReviewInputDTO(4, 7, 4, "Review", "  "));
    }

    private void assertInvalid(ProductReviewInputDTO review) throws Exception {
        try {
            service.avaliaProduct(review);
            fail("Expected invalid review data to be rejected.");
        } catch (StoreException expected) {
            verify(reviewData, never()).insereReviewDoProduct(review);
        }
    }
}
