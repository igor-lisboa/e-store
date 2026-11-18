package br.uff.store.core.interfaces.data;

import java.util.List;

import br.uff.store.core.dtos.ProductReviewInputDTO;
import br.uff.store.core.dtos.ProductReviewListDTO;
import br.uff.store.core.exceptions.StoreException;

public interface IProductReviewData {

    public Boolean jaFoiAvaliado(Integer usuarioId, Integer produtoId) throws StoreException;

    public void insereReviewDoProduct(ProductReviewInputDTO avaliacaoProductInsertDTO) throws StoreException;

    public List<ProductReviewListDTO> recuperaAvaliacoesDeUmProduct(Integer produtoId) throws StoreException;
}
