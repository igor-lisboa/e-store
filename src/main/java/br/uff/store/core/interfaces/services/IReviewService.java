package br.uff.store.core.interfaces.services;

import java.util.List;

import br.uff.store.core.dtos.ProductReviewInputDTO;
import br.uff.store.core.dtos.ProductReviewListDTO;
import br.uff.store.core.exceptions.StoreException;

public interface IReviewService {

    public void reviewProduct(ProductReviewInputDTO reviewInput) throws StoreException;

    public List<ProductReviewListDTO> listProductReviews(Integer productId) throws StoreException;

    public ProductReviewInputDTO findUserReview(Integer userId, Integer productId) throws StoreException;

    /** A customer changes only their own review; the user ID must come from the session. */
    public void updateReview(ProductReviewInputDTO review) throws StoreException;

    /** A customer removes only their own review; the user ID must come from the session. */
    public void deleteReview(Integer userId, Integer productId) throws StoreException;
}
