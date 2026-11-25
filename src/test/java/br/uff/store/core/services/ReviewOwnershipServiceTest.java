package br.uff.store.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.uff.store.core.dtos.ProductReviewInputDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IProductReviewData;
import org.junit.Before;
import org.junit.Test;

/** Decision 10C: customers edit and delete only their own reviews. */
public class ReviewOwnershipServiceTest {
    private IProductReviewData reviewData;
    private ReviewService service;

    @Before
    public void setUp() {
        reviewData = mock(IProductReviewData.class);
        service = new ReviewService(reviewData);
    }

    @Test
    public void updatingOwnReviewReachesPersistenceWithTheCallersIdentity() throws Exception {
        ProductReviewInputDTO review = new ProductReviewInputDTO(4, 7, 3, "Changed", "New title");
        when(reviewData.updateProductReview(review)).thenReturn(1);

        service.updateReview(review);

        verify(reviewData).updateProductReview(review);
    }

    @Test
    public void updatingAReviewThatDoesNotBelongToTheUserFailsInsteadOfSilentlySucceeding() throws Exception {
        ProductReviewInputDTO review = new ProductReviewInputDTO(4, 7, 3, "Changed", "New title");
        when(reviewData.updateProductReview(review)).thenReturn(0);

        try {
            service.updateReview(review);
            fail("A missing or foreign review must be reported.");
        } catch (StoreException expected) {
            assertEquals("You have no review of this product to change.", expected.getMessage());
        }
    }

    @Test
    public void invalidUpdatesNeverReachPersistence() throws Exception {
        ProductReviewInputDTO[] invalid = {
            new ProductReviewInputDTO(4, 7, 0, "Text", "Title"),
            new ProductReviewInputDTO(4, 7, 6, "Text", "Title"),
            new ProductReviewInputDTO(4, 7, 4, "Text", "   "),
            new ProductReviewInputDTO(null, 7, 4, "Text", "Title"),
            new ProductReviewInputDTO(4, 7, 4, new String(new char[256]).replace('\0', 'x'), "Title")
        };
        for (ProductReviewInputDTO review : invalid) {
            try {
                service.updateReview(review);
                fail("Invalid review must be rejected.");
            } catch (StoreException expected) {
                // Expected.
            }
        }
        verify(reviewData, never()).updateProductReview(any());
    }

    @Test
    public void deletingOwnReviewIsDelegatedByUserAndProduct() throws Exception {
        when(reviewData.deleteProductReview(4, 7)).thenReturn(1);

        service.deleteReview(4, 7);

        verify(reviewData).deleteProductReview(4, 7);
    }

    @Test
    public void deletingSomeoneElsesOrMissingReviewIsReported() throws Exception {
        when(reviewData.deleteProductReview(4, 7)).thenReturn(0);

        try {
            service.deleteReview(4, 7);
            fail("Nothing was deleted, so the caller must be told.");
        } catch (StoreException expected) {
            assertEquals("You have no review of this product to delete.", expected.getMessage());
        }
    }

    @Test
    public void invalidIdentifiersNeverReachDeletionOrLookup() throws Exception {
        Integer[][] invalid = {{null, 7}, {4, null}, {0, 7}, {4, -3}};
        for (Integer[] ids : invalid) {
            try {
                service.deleteReview(ids[0], ids[1]);
                fail("Invalid identifiers must be rejected.");
            } catch (StoreException expected) {
                // Expected.
            }
            try {
                service.findUserReview(ids[0], ids[1]);
                fail("Invalid identifiers must be rejected.");
            } catch (StoreException expected) {
                // Expected.
            }
        }
        verify(reviewData, never()).deleteProductReview(anyInt(), anyInt());
        verify(reviewData, never()).findUserReview(anyInt(), anyInt());
    }

    @Test
    public void lookupReturnsTheUsersOwnReview() throws Exception {
        ProductReviewInputDTO own = new ProductReviewInputDTO(4, 7, 5, "Text", "Title");
        when(reviewData.findUserReview(4, 7)).thenReturn(own);

        assertSame(own, service.findUserReview(4, 7));
    }
}
