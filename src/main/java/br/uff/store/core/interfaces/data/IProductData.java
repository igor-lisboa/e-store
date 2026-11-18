package br.uff.store.core.interfaces.data;

import java.util.List;

import br.uff.store.core.dtos.ProductFilterDTO;
import br.uff.store.core.dtos.PaginateDTO;
import br.uff.store.core.dtos.ProductDTO;
import br.uff.store.core.dtos.FeaturedProductDTO;
import br.uff.store.core.dtos.ProductListDTO;
import br.uff.store.core.dtos.CustomerProductDTO;
import br.uff.store.core.exceptions.StoreException;

public interface IProductData {

    public ProductDTO encontraProductPorId(Integer id) throws StoreException;

    public void excluiProductPorId(Integer id) throws StoreException;

    public void atualizaProductPorId(Integer id, ProductDTO produto) throws StoreException;

    public void insereQuantidadeEmEstoqueDoProductPorId(Integer id, Integer quantidade) throws StoreException;

    public void insereProduct(ProductDTO produto) throws StoreException;

    public List<ProductDTO> listaProductsAdm() throws StoreException;

    public List<FeaturedProductDTO> listaProductsBanner() throws StoreException;

    public PaginateDTO<List<ProductListDTO>> listaProductsVitrine(ProductFilterDTO filtro) throws StoreException;

    public CustomerProductDTO mostraProductVitrineParaUser(Integer id, Integer usuarioId) throws StoreException;

    public Boolean produtoFavoritadoPeloUser(Integer produtoId, Integer usuarioId) throws StoreException;

    public void removeFavoritacaoProductPeloUser(Integer produtoId, Integer usuarioId) throws StoreException;

    public void adicionaFavoritacaoProductPeloUser(Integer produtoId, Integer usuarioId) throws StoreException;
}
