package br.uff.store.core.services;

import java.util.List;

import br.uff.store.core.dtos.ProductFilterDTO;
import br.uff.store.core.dtos.PaginateDTO;
import br.uff.store.core.dtos.ProductDTO;
import br.uff.store.core.dtos.FeaturedProductDTO;
import br.uff.store.core.dtos.ProductListDTO;
import br.uff.store.core.dtos.CustomerProductDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IProductData;
import br.uff.store.core.interfaces.services.IProductService;
import br.uff.store.infrastructure.data.ProductData;

public class ProductService implements IProductService {

    private final IProductData produtoData;

    public ProductService() {
        this(new ProductData());
    }

    public ProductService(IProductData produtoData) {
        if (produtoData == null) {
            throw new IllegalArgumentException("Product data dependency is required.");
        }
        this.produtoData = produtoData;
    }

    @Override
    public ProductDTO encontraProductPorId(Integer id) throws StoreException {
        return produtoData.encontraProductPorId(id);
    }

    @Override
    public void excluiProductPorId(Integer id) throws StoreException {
        produtoData.excluiProductPorId(id);
    }

    @Override
    public ProductDTO atualizaProductPorId(Integer id, ProductDTO produto) throws StoreException {
        produtoData.atualizaProductPorId(id, produto);
        return produtoData.encontraProductPorId(id);
    }

    @Override
    public void insereQuantidadeEmEstoqueDoProductPorId(Integer id, Integer quantidade) throws StoreException {
        produtoData.insereQuantidadeEmEstoqueDoProductPorId(id, quantidade);
    }

    @Override
    public void insereProduct(ProductDTO produto) throws StoreException {
        produtoData.insereProduct(produto);
    }

    @Override
    public void usuarioToogleFavoritaProductPorId(Integer produtoId, Integer usuarioId) throws StoreException {
        if (Boolean.TRUE.equals(this.produtoFavoritadoPeloUser(produtoId, usuarioId))) {
            produtoData.removeFavoritacaoProductPeloUser(produtoId, usuarioId);
        } else {
            produtoData.adicionaFavoritacaoProductPeloUser(produtoId, usuarioId);
        }
    }

    @Override
    public List<ProductDTO> listaProductsAdm() throws StoreException {
        return produtoData.listaProductsAdm();
    }

    @Override
    public PaginateDTO<List<ProductListDTO>> listaProductsVitrine(ProductFilterDTO filtro) throws StoreException {
        return produtoData.listaProductsVitrine(filtro);
    }

    @Override
    public CustomerProductDTO mostraProductVitrineParaUser(Integer id, Integer usuarioId) throws StoreException {
        return produtoData.mostraProductVitrineParaUser(id, usuarioId);
    }

    @Override
    public List<FeaturedProductDTO> listaProductsBanner() throws StoreException {
        return produtoData.listaProductsBanner();
    }

    @Override
    public Boolean produtoFavoritadoPeloUser(Integer produtoId, Integer usuarioId) throws StoreException {
        return produtoData.produtoFavoritadoPeloUser(produtoId, usuarioId);
    }
}
