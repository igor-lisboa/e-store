package br.uff.store.core.services;

import br.uff.store.core.dtos.ProductReviewInputDTO;
import br.uff.store.core.dtos.ProductReviewListDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IProductReviewData;
import br.uff.store.core.interfaces.services.IReviewService;
import br.uff.store.infrastructure.data.ProductReviewData;
import java.util.List;

public class ReviewService implements IReviewService {
    private final IProductReviewData avaliacaoProductData;

    public ReviewService() {
        this(new ProductReviewData());
    }

    public ReviewService(IProductReviewData avaliacaoProductData) {
        if (avaliacaoProductData == null) {
            throw new IllegalArgumentException("Review data dependency is required.");
        }
        this.avaliacaoProductData = avaliacaoProductData;
    }

    @Override
    public void avaliaProduct(ProductReviewInputDTO review) throws StoreException {
        validate(review);
        if (Boolean.FALSE.equals(avaliacaoProductData.jaFoiAvaliado(review.getUserId(), review.getProductId()))) {
            avaliacaoProductData.insereReviewDoProduct(review);
            return;
        }
        throw new StoreException("This user has already reviewed this product.");
    }

    private void validate(ProductReviewInputDTO review) throws StoreException {
        if (review == null || review.getUserId() == null || review.getUserId() <= 0
                || review.getProductId() == null || review.getProductId() <= 0) {
            throw new StoreException("A valid user and product are required.");
        }
        if (review.getReview() == null || review.getReview() < 1 || review.getReview() > 5) {
            throw new StoreException("A rating must be between 1 and 5.");
        }
        if (review.getTitulo() == null || review.getTitulo().trim().isEmpty() || review.getTitulo().length() > 255
                || (review.getDescricao() != null && review.getDescricao().length() > 255)) {
            throw new StoreException("Review title and description must be valid and no longer than 255 characters.");
        }
    }

    @Override
    public List<ProductReviewListDTO> recuperaAvaliacoesDeUmProduct(Integer produtoId) throws StoreException {
        return avaliacaoProductData.recuperaAvaliacoesDeUmProduct(produtoId);
    }
}
