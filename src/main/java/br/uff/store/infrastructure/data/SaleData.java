package br.uff.store.infrastructure.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.uff.store.core.dtos.PaginateDTO;
import br.uff.store.core.dtos.SaleDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.ISaleData;
import br.uff.store.infrastructure.database.MySQLDAO;

public class SaleData implements ISaleData {

    private final MySQLDAO mysqlDAO;

    private static final String CONSULTAVENDASDOUSUARIO = "SELECT id, cart_id AS carrinhoId, total_price AS precoTotal, created_at AS criadoEm, address_id AS enderecoId, user_id AS usuarioId FROM sales WHERE user_id=? ORDER BY id DESC";

    public SaleData() {
        this.mysqlDAO = new MySQLDAO();
    }

    @Override
    public List<SaleDTO> listaSalesDoUser(Integer usuarioId) throws StoreException {
        try {
            Object[] bind = {usuarioId};
            List<HashMap<String, Object>> retornoDesformatado = this.mysqlDAO.dbCarrega(CONSULTAVENDASDOUSUARIO, bind);
            List<SaleDTO> retornoFormatado = new ArrayList<>();
            retornoDesformatado.forEach(venda -> retornoFormatado.add(new SaleDTO(venda)));
            return retornoFormatado;
        } catch (Exception e) {
            throw new StoreException("Falha ao Listar as Sales do usuário de id: " + usuarioId + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public void gravaSale(Integer carrinhoId, Integer enderecoId) throws StoreException {
        try {
            Object[] bind = {carrinhoId, enderecoId};
            this.mysqlDAO.dbGrava("CALL buy_cart_itens(?, ?)", bind, false);
        } catch (Exception e) {
            throw new StoreException("Falha ao realizar a venda. (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public PaginateDTO<List<SaleDTO>> listaSalesDoUser(Integer usuarioId, Integer itensPorPagina, Integer paginaAtual) throws StoreException {
        try {
            Object[] bind = {usuarioId};

            // define offset
            String offset = "";
            if (paginaAtual > 1) {
                Integer calcOffset = (paginaAtual - 1) * itensPorPagina;
                offset = " OFFSET " + calcOffset + " ";
            }

            String limit = " LIMIT " + itensPorPagina + " ";

            List<HashMap<String, Object>> retornoDesformatado = this.mysqlDAO.dbCarrega(CONSULTAVENDASDOUSUARIO + limit + offset, bind);
            List<SaleDTO> retornoFormatado = new ArrayList<>();
            retornoDesformatado.forEach(venda -> retornoFormatado.add(new SaleDTO(venda)));

            Integer ultimaPagina = Integer.valueOf(String.valueOf(this.mysqlDAO.dbValor("ceil(count(*)/" + itensPorPagina + ")", CONSULTAVENDASDOUSUARIO, "", bind)));

            return new PaginateDTO<>(paginaAtual, retornoFormatado, ultimaPagina);
        } catch (Exception e) {
            throw new StoreException("Falha ao Listar as Sales do usuário de id: " + usuarioId + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }
}
