package br.uff.store.core.interfaces.data;

import java.util.List;

import br.uff.store.core.dtos.ProductReviewInputDTO;
import br.uff.store.core.dtos.ProductReviewListDTO;
import br.uff.store.core.exceptions.StoreException;

public interface IProductReviewData {

    public Boolean hasReviewedProduct(Integer userId, Integer productId) throws StoreException;

    public void insertProductReview(ProductReviewInputDTO reviewInput) throws StoreException;

    public List<ProductReviewListDTO> listProductReviews(Integer productId) throws StoreException;
}
