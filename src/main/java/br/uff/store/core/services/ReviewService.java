package br.uff.store.core.services;

import br.uff.store.core.dtos.ProductReviewInputDTO;
import br.uff.store.core.dtos.ProductReviewListDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IProductReviewData;
import br.uff.store.core.interfaces.services.IReviewService;
import java.util.List;

public class ReviewService implements IReviewService {
    private final IProductReviewData reviewData;

    public ReviewService(IProductReviewData reviewData) {
        if (reviewData == null) {
            throw new IllegalArgumentException("Review data dependency is required.");
        }
        this.reviewData = reviewData;
    }

    @Override
    public void reviewProduct(ProductReviewInputDTO review) throws StoreException {
        validate(review);
        if (Boolean.FALSE.equals(reviewData.hasReviewedProduct(review.getUserId(), review.getProductId()))) {
            reviewData.insertProductReview(review);
            return;
        }
        throw new StoreException("This user has already reviewed this product.");
    }

    @Override
    public ProductReviewInputDTO findUserReview(Integer userId, Integer productId) throws StoreException {
        InputRules.positiveId(userId, "User ID");
        InputRules.positiveId(productId, "Product ID");
        return reviewData.findUserReview(userId, productId);
    }

    @Override
    public void updateReview(ProductReviewInputDTO review) throws StoreException {
        validate(review);
        if (reviewData.updateProductReview(review) == 0) {
            throw new StoreException("You have no review of this product to change.");
        }
    }

    @Override
    public void deleteReview(Integer userId, Integer productId) throws StoreException {
        InputRules.positiveId(userId, "User ID");
        InputRules.positiveId(productId, "Product ID");
        if (reviewData.deleteProductReview(userId, productId) == 0) {
            throw new StoreException("You have no review of this product to delete.");
        }
    }

    private void validate(ProductReviewInputDTO review) throws StoreException {
        if (review == null || review.getUserId() == null || review.getUserId() <= 0
                || review.getProductId() == null || review.getProductId() <= 0) {
            throw new StoreException("A valid user and product are required.");
        }
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new StoreException("A rating must be between 1 and 5.");
        }
        if (review.getTitle() == null || review.getTitle().trim().isEmpty() || review.getTitle().length() > 255
                || (review.getDescription() != null && review.getDescription().length() > 255)) {
            throw new StoreException("Review title and description must be valid and no longer than 255 characters.");
        }
    }

    @Override
    public List<ProductReviewListDTO> listProductReviews(Integer productId) throws StoreException {
        return reviewData.listProductReviews(productId);
    }
}
