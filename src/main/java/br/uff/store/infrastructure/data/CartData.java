package br.uff.store.infrastructure.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.uff.store.core.dtos.CartDTO;
import br.uff.store.core.dtos.CartItemDTO;
import br.uff.store.core.dtos.PaginateDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.ICartData;
import br.uff.store.infrastructure.database.MySQLDAO;
import java.util.Arrays;

public class CartData implements ICartData {

    private final MySQLDAO mysqlDAO;

    private static final String COUNTSTRING = "count(*)";
    private static final String CARTTABLESTRING = "carts";
    private static final String CARTSPRODUCTSTABLESTRING = "carts_products";
    private static final String ERROPREFIXOSTRING = "Falha ao verificar se o Cart de id: ";
    private static final String ERROESPECIFICANDOCARRINHO = " no Cart de id: ";
    private static final String CONSULTAPRODUTOSCARRINHO = "SELECT c.cart_id AS id, p.id AS produtoId, p.name AS nome, p.description AS descricao, p.img AS imagem, p.price AS preco, p.quantity AS quantidadeEmEstoque, c.quantity AS quantidade, p.price * c.quantity AS precoTotal FROM carts_products c LEFT JOIN products p ON (c.product_id = p.id) WHERE c.cart_id=?";

    public CartData() {
        this.mysqlDAO = new MySQLDAO();
    }

    @Override
    public CartDTO encontraCart(Integer id) throws StoreException {
        try {
            Object[] bind = {id};
            List<HashMap<String, Object>> retorno = this.mysqlDAO.dbCarrega("SELECT id, user_id AS usuarioId, created_at AS criadoEm, ip FROM carts WHERE id=?", bind);
            return new CartDTO(retorno.get(0));
        } catch (Exception e) {
            throw new StoreException("Falha ao Recuperar o Cart de id: " + id + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public List<CartDTO> listaCartsDoUserNaoVendidos(Integer usuarioId) throws StoreException {
        try {
            Object[] bind = {usuarioId};
            List<HashMap<String, Object>> retornoDesformatado = this.mysqlDAO.dbCarrega("SELECT id, user_id AS usuarioId, created_at AS criadoEm, ip FROM carts WHERE user_id=? AND id NOT IN (SELECT cart_id FROM sales) ORDER BY id DESC", bind);
            List<CartDTO> retornoFormatado = new ArrayList<>();
            retornoDesformatado.forEach(carrinho -> retornoFormatado.add(new CartDTO(carrinho)));
            return retornoFormatado;
        } catch (Exception e) {
            throw new StoreException("Falha ao Listar os Carts não vendidos do usuário de id: " + usuarioId + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public Boolean carrinhoVendido(Integer id) throws StoreException {
        try {
            Object[] bind = {id};
            return Integer.valueOf(String.valueOf(this.mysqlDAO.dbValor(COUNTSTRING, CARTTABLESTRING, "id=? AND id IN (SELECT cart_id FROM sales)", bind))) > 0;
        } catch (Exception e) {
            throw new StoreException(ERROPREFIXOSTRING + id + " já foi vendido. (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public void defineDonoDeUmCartSemUserNoMomento(Integer id, Integer usuarioId) throws StoreException {
        try {
            Object[] bind = {usuarioId, id};
            this.mysqlDAO.dbGrava("UPDATE carts SET user_id=? WHERE id=? AND user_id IS NULL", bind, false);
        } catch (Exception e) {
            throw new StoreException("Falha ao Atualizar o Dono do Cart de id: " + id + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public Boolean carrinhoSemDono(Integer id) throws StoreException {
        try {
            Object[] bind = {id};
            return Integer.valueOf(String.valueOf(this.mysqlDAO.dbValor(COUNTSTRING, CARTTABLESTRING, "id=? AND user_id IS NULL", bind))) > 0;
        } catch (Exception e) {
            throw new StoreException(ERROPREFIXOSTRING + id + " está sem dono. (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public Boolean carrinhoDoUser(Integer id, Integer usuarioId) throws StoreException {
        try {
            Object[] bind = {id, usuarioId};
            return Integer.valueOf(String.valueOf(this.mysqlDAO.dbValor(COUNTSTRING, CARTTABLESTRING, "id=? AND user_id=?", bind))) > 0;
        } catch (Exception e) {
            throw new StoreException(ERROPREFIXOSTRING + id + " é do usuário de id: " + usuarioId + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public Boolean carrinhoExiste(Integer id) throws StoreException {
        try {
            Object[] bind = {id};
            return Integer.valueOf(String.valueOf(this.mysqlDAO.dbValor(COUNTSTRING, CARTTABLESTRING, "id=?", bind))) > 0;
        } catch (Exception e) {
            throw new StoreException(ERROPREFIXOSTRING + id + " existe. (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public Integer quantidadeProductsCart(Integer id) throws StoreException {
        try {
            Object[] bind = {id};
            return Integer.valueOf(String.valueOf(this.mysqlDAO.dbValor(COUNTSTRING, CARTSPRODUCTSTABLESTRING, "cart_id=?", bind)));
        } catch (Exception e) {
            throw new StoreException("Falha ao verificar a quantidade de produtos no Cart de id: " + id + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public Integer quantidadeProductNoCart(Integer id, Integer produtoId) throws StoreException {
        try {
            Object[] bind = {produtoId, id};
            String quantity = String.valueOf(this.mysqlDAO.dbValor("quantity", CARTSPRODUCTSTABLESTRING, "product_id=? AND cart_id=?", bind));
            return Integer.valueOf(quantity.equals("null") ? "0" : quantity);
        } catch (Exception e) {
            throw new StoreException("Falha ao verificar a quantidade do produto de id: " + produtoId + ERROESPECIFICANDOCARRINHO + id + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public void criaCart(String ip, String criadoEm, Integer usuarioId) throws StoreException {
        try {
            Object[] bind = {ip, criadoEm, usuarioId};
            this.mysqlDAO.dbGrava("INSERT INTO carts (ip,created_at,user_id) VALUES (?,?,?)", bind, false);
        } catch (Exception e) {
            throw new StoreException("Falha ao Inserir o Cart de para o Usuário de id: " + usuarioId + " e ip: " + ip + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public CartDTO encontraCart(String ip, String criadoEm, Integer usuarioId) throws StoreException {
        try {
            ArrayList<Object> bind = new ArrayList<Object>(Arrays.asList(ip, criadoEm));
            if (usuarioId != null) {
                bind.add(usuarioId);
            }
            List<HashMap<String, Object>> retorno = this.mysqlDAO.dbCarrega("SELECT id, user_id AS usuarioId, created_at AS criadoEm, ip FROM carts WHERE ip=? AND created_at=? AND user_id" + (usuarioId == null ? " IS NULL " : "=?") + " ORDER BY id DESC LIMIT 1", bind.toArray());
            return new CartDTO(retorno.get(0));
        } catch (Exception e) {
            throw new StoreException("Falha ao Recuperar o Cart de ip: " + ip + " do usuário de id: " + usuarioId + " que foi criado em " + criadoEm + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public List<CartItemDTO> listaProductsCart(Integer id) throws StoreException {
        try {
            Object[] bind = {id};
            List<HashMap<String, Object>> retornoDesformatado = this.mysqlDAO.dbCarrega(CONSULTAPRODUTOSCARRINHO, bind);
            List<CartItemDTO> retornoFormatado = new ArrayList<>();
            retornoDesformatado.forEach(carrinho -> retornoFormatado.add(new CartItemDTO(carrinho)));
            return retornoFormatado;
        } catch (Exception e) {
            throw new StoreException("Falha ao Listar os Products do Cart de id: " + id + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public Boolean verificaProductNoCart(Integer id, Integer produtoId) throws StoreException {
        try {
            Object[] bind = {produtoId, id};
            return Integer.valueOf(String.valueOf(this.mysqlDAO.dbValor(COUNTSTRING, CARTSPRODUCTSTABLESTRING, "product_id=? AND cart_id=?", bind))) > 0;
        } catch (Exception e) {
            throw new StoreException("Falha ao verificar se o produto de id: " + produtoId + " está no Cart de id: " + id + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public void adicionaProductNoCart(Integer id, Integer produtoId) throws StoreException {
        try {
            Object[] bind = {produtoId, id};
            this.mysqlDAO.dbGrava("INSERT INTO carts_products (product_id,cart_id,quantity) VALUES (?,?,1)", bind, false);
        } catch (Exception e) {
            throw new StoreException("Falha ao Inserir o Product de id: " + produtoId + ERROESPECIFICANDOCARRINHO + id + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public void removeProductDoCart(Integer id, Integer produtoId) throws StoreException {
        try {
            Object[] bind = {produtoId, id};
            this.mysqlDAO.dbGrava("DELETE FROM carts_products WHERE product_id=? AND cart_id=?", bind, false);
        } catch (Exception e) {
            throw new StoreException("Falha ao Remover o Product de id: " + produtoId + ERROESPECIFICANDOCARRINHO + id + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public void atualizaQtdDoProductNoCart(Integer id, Integer produtoId, Integer novaQtd) throws StoreException {
        try {
            Object[] bind = {novaQtd, produtoId, id};
            this.mysqlDAO.dbGrava("UPDATE carts_products SET quantity=? WHERE product_id=? AND cart_id=?", bind, false);
        } catch (Exception e) {
            throw new StoreException("Falha ao Atualizar quantidade do Product de id: " + produtoId + ERROESPECIFICANDOCARRINHO + id + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public Double recuperaPrecoTotalDeUmCart(Integer id) throws StoreException {
        try {
            Object[] bind = {id};
            String totalPrice = String.valueOf(this.mysqlDAO.dbValor("precoTotal", "SELECT sum(p.price * c.quantity) AS precoTotal FROM carts_products c LEFT JOIN products p ON (c.product_id = p.id) WHERE c.cart_id=? GROUP BY c.cart_id", "", bind));
            return Double.valueOf(totalPrice.equals("null") ? "0" : totalPrice);
        } catch (Exception e) {
            throw new StoreException("Falha ao verificar o valor total do carinho de id: " + id + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public PaginateDTO<List<CartItemDTO>> listaProductsCart(Integer id, Integer itensPorPagina, Integer paginaAtual) throws StoreException {
        try {
            Object[] bind = {id};

            // define offset
            String offset = "";
            if (paginaAtual > 1) {
                Integer calcOffset = (paginaAtual - 1) * itensPorPagina;
                offset = " OFFSET " + calcOffset + " ";
            }

            String limit = " LIMIT " + itensPorPagina + " ";

            List<HashMap<String, Object>> retornoDesformatado = this.mysqlDAO.dbCarrega(CONSULTAPRODUTOSCARRINHO + limit + offset, bind);
            List<CartItemDTO> retornoFormatado = new ArrayList<>();
            retornoDesformatado.forEach(carrinho -> retornoFormatado.add(new CartItemDTO(carrinho)));

            Integer ultimaPagina = Integer.valueOf(String.valueOf(this.mysqlDAO.dbValor("ceil(count(*)/" + itensPorPagina + ")", CONSULTAPRODUTOSCARRINHO, "", bind)));

            return new PaginateDTO<>(paginaAtual, retornoFormatado, ultimaPagina);
        } catch (Exception e) {
            throw new StoreException("Falha ao Paginar e recuperar a página " + paginaAtual + " os Products do Cart de id: " + id + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

}
