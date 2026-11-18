package br.uff.store.core.interfaces.data;

import java.util.List;

import br.uff.store.core.dtos.ProductFilterDTO;
import br.uff.store.core.dtos.PaginationDTO;
import br.uff.store.core.dtos.ProductDTO;
import br.uff.store.core.dtos.FeaturedProductDTO;
import br.uff.store.core.dtos.ProductListDTO;
import br.uff.store.core.dtos.CustomerProductDTO;
import br.uff.store.core.exceptions.StoreException;

public interface IProductData {

    public ProductDTO findProductById(Integer id) throws StoreException;

    public void deleteProductById(Integer id) throws StoreException;

    public void updateProductById(Integer id, ProductDTO product) throws StoreException;

    public void addProductStock(Integer id, Integer quantity) throws StoreException;

    public void insertProduct(ProductDTO product) throws StoreException;

    public List<ProductDTO> listAdminProducts() throws StoreException;

    public List<FeaturedProductDTO> listFeaturedProducts() throws StoreException;

    public PaginationDTO<List<ProductListDTO>> listStorefrontProducts(ProductFilterDTO filter) throws StoreException;

    public CustomerProductDTO getCustomerProduct(Integer id, Integer userId) throws StoreException;

    public Boolean isFavorite(Integer productId, Integer userId) throws StoreException;

    public void removeFavorite(Integer productId, Integer userId) throws StoreException;

    public void addFavorite(Integer productId, Integer userId) throws StoreException;
}
