package br.uff.store.infrastructure.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.uff.store.core.dtos.ProductFilterDTO;
import br.uff.store.core.dtos.PaginateDTO;
import br.uff.store.core.dtos.ProductDTO;
import br.uff.store.core.dtos.FeaturedProductDTO;
import br.uff.store.core.dtos.ProductListDTO;
import br.uff.store.core.dtos.CustomerProductDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IProductData;
import br.uff.store.infrastructure.database.MySQLDAO;

public class ProductData implements IProductData {

    private final MySQLDAO mysqlDAO;
    private static final String CONSULTAPRODUTO = "SELECT p.id AS id, p.name AS nome, p.price AS preco, p.description AS descricao,p.img AS imagem, p.category_id AS categoriaId, p.quantity AS quantidade, c.category_name AS categoria FROM products p LEFT JOIN vw_category c ON (p.category_id = c.id)";

    public ProductData() {
        this.mysqlDAO = new MySQLDAO();
    }

    @Override
    public ProductDTO encontraProductPorId(Integer id) throws StoreException {
        try {
            Object[] bind = { id };
            List<HashMap<String, Object>> retorno = this.mysqlDAO.dbCarrega(CONSULTAPRODUTO + " WHERE p.id=?", bind);
            return new ProductDTO(retorno.get(0));
        } catch (Exception e) {
            throw new StoreException("Falha ao Recuperar o Product de id: " + id + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public void excluiProductPorId(Integer id) throws StoreException {
        try {
            Object[] bind = { id };
            this.mysqlDAO.dbGrava("DELETE FROM products WHERE id=?", bind, false);
        } catch (Exception e) {
            throw new StoreException("Falha ao Excluir o Product de id: " + id + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public void atualizaProductPorId(Integer id, ProductDTO produto) throws StoreException {
        try {
            Object[] bind = { produto.getNome(), produto.getPreco(), produto.getDescricao(), produto.getImagem(),
                    produto.getCategoryId(), id };
            this.mysqlDAO.dbGrava("UPDATE products SET name=?, price=?, description=?, img=?, category_id=? WHERE id=?",
                    bind, false);
        } catch (Exception e) {
            throw new StoreException("Falha ao Atualizar o Product de id: " + id + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public void insereQuantidadeEmEstoqueDoProductPorId(Integer id, Integer quantidade) throws StoreException {
        try {
            Object[] bind = { quantidade, id };
            this.mysqlDAO.dbGrava("UPDATE products SET quantity = quantity + ? WHERE id=?", bind, false);
        } catch (Exception e) {
            throw new StoreException("Falha ao Inserir estoque do Product de id: " + id + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public void insereProduct(ProductDTO produto) throws StoreException {
        try {
            Object[] bind = { produto.getNome(), produto.getPreco(), produto.getDescricao(), produto.getImagem(),
                    produto.getCategoryId() };
            this.mysqlDAO.dbGrava(
                    "INSERT INTO products (name,price,description,img,category_id,created_at,quantity) VALUES (?,?,?,?,?,SYSDATE(),0)",
                    bind, false);
        } catch (Exception e) {
            throw new StoreException("Falha ao Inserir Product. (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public List<ProductDTO> listaProductsAdm() throws StoreException {
        try {
            Object[] bind = {};
            List<HashMap<String, Object>> retornoDesformatado = this.mysqlDAO.dbCarrega(CONSULTAPRODUTO, bind);
            List<ProductDTO> retornoFormatado = new ArrayList<>();
            retornoDesformatado.forEach(produto -> retornoFormatado.add(new ProductDTO(produto)));
            return retornoFormatado;
        } catch (Exception e) {
            throw new StoreException("Falha ao Listar os Products para ADMs. (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public PaginateDTO<List<ProductListDTO>> listaProductsVitrine(ProductFilterDTO filtroProduct)
            throws StoreException {
        try {
            if (filtroProduct.getPaginaAtual() < 1) {
                filtroProduct.setPaginaAtual(1);
            }

            ArrayList<Object> bind = new ArrayList<>();
            String consulta = "SELECT p.id AS id,p.name AS nome,p.price AS preco,p.img AS imagem,c.category_name AS categoria FROM products p LEFT JOIN vw_category c on(p.category_id=c.id) ";

            String filtro = "";
            String limit = " LIMIT " + filtroProduct.getItensPorPagina() + " ";

            String whereTxt = " WHERE ";
            String andTXT = " AND ";
            String orTxt = " OR ";

            // filtra favoritos
            if (Boolean.TRUE.equals(filtroProduct.getApenasFavoritados())) {
                // se tem usuario logado mostra filtra produtos por favoritos
                if (filtroProduct.getUserId() != null) {
                    filtro += (filtro.equals("") ? whereTxt : andTXT)
                            + " p.id in (SELECT f.product_id FROM favorite_products f WHERE f.user_id=?) ";
                    bind.add(filtroProduct.getUserId());
                } else {
                    throw new StoreException("Sem usuário logado, para buscar produtos favoritos realize login.");
                }
            }

            // filtra categorias
            if (filtroProduct.getCategorys() != null && !filtroProduct.getCategorys().isEmpty()) {
                StringBuilder categoriaFiltro = new StringBuilder();
                for (String value : filtroProduct.getCategorys()) {
                    bind.add("%" + value.toUpperCase() + "%");
                    bind.add(value);
                    categoriaFiltro.append((categoriaFiltro.toString().equals("") ? " ( " : orTxt)
                            + " UPPER(c.category_name) LIKE ? OR c.id = ? ");
                }
                categoriaFiltro.append((categoriaFiltro.toString().equals("") ? "" : " ) "));
                filtro += (filtro.equals("") ? whereTxt : andTXT) + categoriaFiltro.toString();
            }

            // filtra sub categorias
            if (filtroProduct.getSubCategorys() != null && !filtroProduct.getSubCategorys().isEmpty()) {
                StringBuilder subCategoryFiltro = new StringBuilder();
                for (String value : filtroProduct.getSubCategorys()) {
                    bind.add("%" + value.toUpperCase() + "%");
                    bind.add(value);
                    subCategoryFiltro.append((subCategoryFiltro.toString().equals("") ? " ( " : orTxt)
                            + " UPPER(c.category_name) LIKE ? OR c.id = ? ");
                }
                subCategoryFiltro.append((subCategoryFiltro.toString().equals("") ? "" : " ) "));
                filtro += (filtro.equals("") ? whereTxt : andTXT) + subCategoryFiltro.toString();
            }

            if (Boolean.TRUE.equals(filtroProduct.getApenasLancamentos())) {
                filtro += (filtro.equals("") ? whereTxt : andTXT)
                        + " p.created_at >= DATE_SUB(NOW(), INTERVAL 1 MONTH) ";
            }

            if (filtroProduct.getPesquisa() != null) {
                bind.add("%" + filtroProduct.getPesquisa().toUpperCase() + "%");
                bind.add("%" + filtroProduct.getPesquisa().toUpperCase() + "%");
                filtro += (filtro.equals("") ? whereTxt : andTXT)
                        + " (UPPER(p.name) LIKE ? OR UPPER(p.description) LIKE ?) ";
            }

            // recupera apenas produtos que tem no estoque
            filtro += (filtro.equals("") ? whereTxt : andTXT) + " p.quantity>0 ";

            // filtra menor preco
            if (filtroProduct.getPrecoMinimo() != null) {
                bind.add(filtroProduct.getPrecoMinimo());
                filtro += (filtro.equals("") ? whereTxt : andTXT) + " p.price >= ? ";
            }

            // filtra maior preco
            if (filtroProduct.getPrecoMaximo() != null) {
                bind.add(filtroProduct.getPrecoMaximo());
                filtro += (filtro.equals("") ? whereTxt : andTXT) + " p.price <= ? ";
            }

            // define offset
            String offset = "";
            if (filtroProduct.getPaginaAtual() > 1) {
                Integer calcOffset = (filtroProduct.getPaginaAtual() - 1) * filtroProduct.getItensPorPagina();
                offset = " OFFSET " + calcOffset + " ";
            }

            List<HashMap<String, Object>> retornoDesformatado = this.mysqlDAO
                    .dbCarrega(consulta + filtro + limit + offset, bind.toArray());
            List<ProductListDTO> retornoFormatado = new ArrayList<>();
            retornoDesformatado.forEach(produto -> retornoFormatado.add(new ProductListDTO(produto)));

            Integer ultimaPagina = Integer.valueOf(
                    String.valueOf(this.mysqlDAO.dbValor("ceil(count(*)/" + filtroProduct.getItensPorPagina() + ")",
                            consulta + filtro, "", bind.toArray())));

            if (ultimaPagina < 1) {
                ultimaPagina = 1;
            }

            if (retornoFormatado.isEmpty()
                    && Boolean.FALSE.equals(filtroProduct.getPaginaAtual().equals(ultimaPagina))) {
                filtroProduct.setPaginaAtual(ultimaPagina);
                return this.listaProductsVitrine(filtroProduct);
            }

            return new PaginateDTO<>(filtroProduct.getPaginaAtual(), retornoFormatado, ultimaPagina);
        } catch (Exception e) {
            throw new StoreException("Falha ao Listar os Products. (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public CustomerProductDTO mostraProductVitrineParaUser(Integer id, Integer usuarioId)
            throws StoreException {
        try {
            Object[] bind = { String.valueOf(usuarioId), String.valueOf(usuarioId), id };
            List<HashMap<String, Object>> retorno = this.mysqlDAO.dbCarrega(
                    "SELECT p.id, p.name AS nome, p.quantity AS quantidade, p.description AS descricao, p.price AS preco, p.img AS imagem, c.category_name AS categoria, (SELECT count(*) FROM favorite_products f WHERE f.product_id = p.id AND f.user_id=?) favoritoDoUser, (SELECT f.rating FROM user_produts_rating f WHERE f.product_id = p.id AND f.user_id=?) avaliacaoDadaPeloUser, (SELECT count(*) FROM user_produts_rating f WHERE f.product_id = p.id) quantidadeAvaliacoes, (SELECT COALESCE(sum(f.rating),0) FROM user_produts_rating f WHERE f.product_id = p.id) somaAvaliacoes, (SELECT count(*) FROM user_produts_rating f WHERE f.product_id = p.id AND f.rating='1') quantidadeAvaliacoesNota1, (SELECT count(*) FROM user_produts_rating f WHERE f.product_id = p.id AND f.rating='2') quantidadeAvaliacoesNota2, (SELECT count(*) FROM user_produts_rating f WHERE f.product_id = p.id AND f.rating='3') quantidadeAvaliacoesNota3, (SELECT count(*) FROM user_produts_rating f WHERE f.product_id = p.id AND f.rating='4') quantidadeAvaliacoesNota4, (SELECT count(*) FROM user_produts_rating f WHERE f.product_id = p.id AND f.rating='5') quantidadeAvaliacoesNota5 FROM products p LEFT JOIN vw_category c ON (p.category_id = c.id) WHERE p.id=?",
                    bind);
            return new CustomerProductDTO(retorno.get(0));
        } catch (Exception e) {
            throw new StoreException("Falha ao mostrar o Product " + id + " para o usuario. (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public Boolean produtoFavoritadoPeloUser(Integer produtoId, Integer usuarioId) throws StoreException {
        try {
            Object[] bind = { produtoId, usuarioId };
            return Integer.valueOf(String.valueOf(
                    this.mysqlDAO.dbValor("count(*)", "favorite_products", "product_id=? AND user_id=?", bind))) > 0;
        } catch (Exception e) {
            throw new StoreException("Falha ao verificar se o produto de id:" + produtoId
                    + " já foi favoritado pelo usuário de id: " + usuarioId + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public void removeFavoritacaoProductPeloUser(Integer produtoId, Integer usuarioId) throws StoreException {
        try {
            Object[] bind = { produtoId, usuarioId };
            this.mysqlDAO.dbGrava("DELETE FROM favorite_products WHERE product_id=? AND user_id=?", bind, false);
        } catch (Exception e) {
            throw new StoreException("Falha ao desfavoritar o produto de id:" + produtoId + " pelo usuário de id: "
                    + usuarioId + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public void adicionaFavoritacaoProductPeloUser(Integer produtoId, Integer usuarioId) throws StoreException {
        try {
            Object[] bind = { produtoId, usuarioId };
            this.mysqlDAO.dbGrava("INSERT INTO favorite_products (product_id,user_id) VALUES (?,?)", bind, false);
        } catch (Exception e) {
            throw new StoreException("Falha ao favoritar o produto de id:" + produtoId + " pelo usuário de id: "
                    + usuarioId + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public List<FeaturedProductDTO> listaProductsBanner() throws StoreException {
        try {
            Object[] bind = {};
            List<HashMap<String, Object>> retornoDesformatado = this.mysqlDAO.dbCarrega(
                    "SELECT id, name AS nome, description AS descricao, img AS imagem FROM products WHERE quantity > 0 ORDER BY quantity DESC LIMIT 3",
                    bind);
            List<FeaturedProductDTO> retornoFormatado = new ArrayList<>();
            retornoDesformatado.forEach(produto -> retornoFormatado.add(new FeaturedProductDTO(produto)));
            return retornoFormatado;
        } catch (Exception e) {
            throw new StoreException("Falha ao listar produtos do banner da Home. (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }
}
