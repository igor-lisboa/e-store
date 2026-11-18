package br.uff.store.core.interfaces.services;

import java.util.List;

import br.uff.store.core.dtos.ProductReviewInputDTO;
import br.uff.store.core.dtos.ProductReviewListDTO;
import br.uff.store.core.exceptions.StoreException;

public interface IReviewService {

    public void reviewProduct(ProductReviewInputDTO reviewInput) throws StoreException;

    public List<ProductReviewListDTO> listProductReviews(Integer productId) throws StoreException;
}
