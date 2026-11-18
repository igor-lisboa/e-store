package br.uff.store.infrastructure.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.uff.store.core.dtos.ProductReviewInputDTO;
import br.uff.store.core.dtos.ProductReviewListDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IProductReviewData;
import br.uff.store.infrastructure.database.MySQLDAO;

public class ProductReviewData implements IProductReviewData {

    private final MySQLDAO mysqlDAO;

    public ProductReviewData() {
        this.mysqlDAO = new MySQLDAO();
    }

    @Override
    public Boolean jaFoiAvaliado(Integer usuarioId, Integer produtoId) throws StoreException {
        try {
            Object[] bind = {usuarioId, produtoId};
            Integer qtdAvaliacoes = Integer.valueOf(String.valueOf(this.mysqlDAO.dbValor("count(*)", "user_produts_rating", "user_id=? AND product_id=?", bind)));
            return qtdAvaliacoes > 0;
        } catch (Exception e) {
            throw new StoreException("Falha ao recuperar quantidade de avaliacoes do usuário para o produto: " + e.getMessage());
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public void insereReviewDoProduct(ProductReviewInputDTO avaliacaoProductInsertDTO) throws StoreException {
        try {
            Object[] bind = {avaliacaoProductInsertDTO.getUserId(), avaliacaoProductInsertDTO.getProductId(), avaliacaoProductInsertDTO.getReview(), avaliacaoProductInsertDTO.getDescricao(), avaliacaoProductInsertDTO.getTitulo()};
            this.mysqlDAO.dbGrava("INSERT INTO user_produts_rating (user_id,product_id,rating,description,title,created_at) VALUES (?,?,?,?,?,SYSDATE())", bind, false);
        } catch (Exception e) {
            throw new StoreException("Falha ao Inserir a Avaliação do usuário de id: " + avaliacaoProductInsertDTO.getUserId() + " para o Product de id: " + avaliacaoProductInsertDTO.getProductId() + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public List<ProductReviewListDTO> recuperaAvaliacoesDeUmProduct(Integer produtoId) throws StoreException {
        try {
            Object[] bind = {produtoId};
            List<HashMap<String, Object>> retornoDesformatado = this.mysqlDAO.dbCarrega("SELECT  u.`name` AS avaliador,  r.title AS avaliacaoTitulo,  r.rating AS avaliacao,  r.description AS avaliacaoDescricao,  DATE_FORMAT(r.created_at, '%M %d, %Y') AS avaliacaoData,  DATE_FORMAT(r.created_at, '%d/%m/%Y') AS avaliacaoDataSimples  FROM  user_produts_rating r  LEFT JOIN users u ON (r.user_id = u.id)  WHERE  r.product_id = ?  ORDER BY  r.created_at", bind);
            List<ProductReviewListDTO> retornoFormatado = new ArrayList<>();
            retornoDesformatado.forEach(avaliacao -> retornoFormatado.add(new ProductReviewListDTO(avaliacao)));
            return retornoFormatado;
        } catch (Exception ex) {
            throw new StoreException("Erro ao recuperar registros do banco: " + ex.getMessage());
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }
}
