package br.uff.loja.core.services;

import br.uff.loja.core.dtos.AvaliacaoProdutoInsertDTO;
import br.uff.loja.core.dtos.AvaliacaoProdutoListDTO;
import br.uff.loja.core.exceptions.LojaException;
import br.uff.loja.core.interfaces.data.IAvaliacaoProdutoData;
import br.uff.loja.core.interfaces.services.IAvaliacaoService;
import br.uff.loja.infrastructure.data.AvaliacaoProdutoData;
import java.util.List;

public class AvaliacaoService implements IAvaliacaoService {
    private final IAvaliacaoProdutoData avaliacaoProdutoData;

    public AvaliacaoService() {
        this(new AvaliacaoProdutoData());
    }

    public AvaliacaoService(IAvaliacaoProdutoData avaliacaoProdutoData) {
        if (avaliacaoProdutoData == null) {
            throw new IllegalArgumentException("Review data dependency is required.");
        }
        this.avaliacaoProdutoData = avaliacaoProdutoData;
    }

    @Override
    public void avaliaProduto(AvaliacaoProdutoInsertDTO review) throws LojaException {
        validate(review);
        if (Boolean.FALSE.equals(avaliacaoProdutoData.jaFoiAvaliado(review.getUsuarioId(), review.getProdutoId()))) {
            avaliacaoProdutoData.insereAvaliacaoDoProduto(review);
            return;
        }
        throw new LojaException("This user has already reviewed this product.");
    }

    private void validate(AvaliacaoProdutoInsertDTO review) throws LojaException {
        if (review == null || review.getUsuarioId() == null || review.getUsuarioId() <= 0
                || review.getProdutoId() == null || review.getProdutoId() <= 0) {
            throw new LojaException("A valid user and product are required.");
        }
        if (review.getAvaliacao() == null || review.getAvaliacao() < 1 || review.getAvaliacao() > 5) {
            throw new LojaException("A rating must be between 1 and 5.");
        }
        if (review.getTitulo() == null || review.getTitulo().trim().isEmpty() || review.getTitulo().length() > 255
                || (review.getDescricao() != null && review.getDescricao().length() > 255)) {
            throw new LojaException("Review title and description must be valid and no longer than 255 characters.");
        }
    }

    @Override
    public List<AvaliacaoProdutoListDTO> recuperaAvaliacoesDeUmProduto(Integer produtoId) throws LojaException {
        return avaliacaoProdutoData.recuperaAvaliacoesDeUmProduto(produtoId);
    }
}
