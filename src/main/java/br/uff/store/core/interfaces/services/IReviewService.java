package br.uff.store.core.interfaces.services;

import java.util.List;

import br.uff.store.core.dtos.ProductReviewInputDTO;
import br.uff.store.core.dtos.ProductReviewListDTO;
import br.uff.store.core.exceptions.StoreException;

public interface IReviewService {

    public void avaliaProduct(ProductReviewInputDTO avaliacaoProductInsertDTO) throws StoreException;

    public List<ProductReviewListDTO> recuperaAvaliacoesDeUmProduct(Integer produtoId) throws StoreException;
}
