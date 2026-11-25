package br.uff.store.core.interfaces.data;

import java.util.List;

import br.uff.store.core.dtos.ProductReviewInputDTO;
import br.uff.store.core.dtos.ProductReviewListDTO;
import br.uff.store.core.exceptions.StoreException;

public interface IProductReviewData {

    public Boolean hasReviewedProduct(Integer userId, Integer productId) throws StoreException;

    public void insertProductReview(ProductReviewInputDTO reviewInput) throws StoreException;

    public List<ProductReviewListDTO> listProductReviews(Integer productId) throws StoreException;

    /** Returns the user's own review of the product, or null when there is none. */
    public ProductReviewInputDTO findUserReview(Integer userId, Integer productId) throws StoreException;

    /** Changes only the review owned by the input's user; returns how many rows changed (0 or 1). */
    public int updateProductReview(ProductReviewInputDTO reviewInput) throws StoreException;

    /** Removes only the review owned by the user; returns how many rows were removed (0 or 1). */
    public int deleteProductReview(Integer userId, Integer productId) throws StoreException;
}
