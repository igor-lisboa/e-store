package br.uff.store.core.services;

import java.util.List;

import br.uff.store.core.dtos.ProductFilterDTO;
import br.uff.store.core.dtos.PaginationDTO;
import br.uff.store.core.dtos.ProductDTO;
import br.uff.store.core.dtos.FeaturedProductDTO;
import br.uff.store.core.dtos.ProductListDTO;
import br.uff.store.core.dtos.CustomerProductDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IProductData;
import br.uff.store.core.interfaces.services.IProductService;

public class ProductService implements IProductService {

    private final IProductData productData;

    public ProductService(IProductData productData) {
        if (productData == null) {
            throw new IllegalArgumentException("Product data dependency is required.");
        }
        this.productData = productData;
    }

    @Override
    public ProductDTO findProductById(Integer id) throws StoreException {
        return productData.findProductById(id);
    }

    @Override
    public void deleteProductById(Integer id) throws StoreException {
        productData.deleteProductById(id);
    }

    @Override
    public ProductDTO updateProductById(Integer id, ProductDTO product) throws StoreException {
        InputRules.positiveId(id, "Product ID");
        validateProduct(product);
        productData.updateProductById(id, product);
        return productData.findProductById(id);
    }

    @Override
    public void addProductStock(Integer id, Integer quantity) throws StoreException {
        InputRules.positiveId(id, "Product ID");
        InputRules.positiveId(quantity, "Stock increment");
        productData.addProductStock(id, quantity);
    }

    @Override
    public void insertProduct(ProductDTO product) throws StoreException {
        validateProduct(product);
        productData.insertProduct(product);
    }

    @Override
    public void toggleFavorite(Integer productId, Integer userId) throws StoreException {
        InputRules.positiveId(productId, "Product ID");
        InputRules.positiveId(userId, "User ID");
        if (Boolean.TRUE.equals(this.isFavorite(productId, userId))) {
            productData.removeFavorite(productId, userId);
        } else {
            productData.addFavorite(productId, userId);
        }
    }

    @Override
    public List<ProductDTO> listAdminProducts() throws StoreException {
        return productData.listAdminProducts();
    }

    @Override
    public PaginationDTO<List<ProductListDTO>> listStorefrontProducts(ProductFilterDTO filter) throws StoreException {
        if (filter == null) {
            throw new StoreException("Product filters are required.");
        }
        InputRules.page(filter.getPageSize(), filter.getPageNumber());
        Double minimum = filter.getMinimumPrice();
        Double maximum = filter.getMaximumPrice();
        validatePriceBound(minimum);
        validatePriceBound(maximum);
        if (minimum != null && maximum != null && minimum > maximum) {
            throw new StoreException("Minimum price cannot exceed maximum price.");
        }
        return productData.listStorefrontProducts(filter);
    }

    @Override
    public CustomerProductDTO getCustomerProduct(Integer id, Integer userId) throws StoreException {
        return productData.getCustomerProduct(id, userId);
    }

    @Override
    public List<FeaturedProductDTO> listFeaturedProducts() throws StoreException {
        return productData.listFeaturedProducts();
    }

    @Override
    public Boolean isFavorite(Integer productId, Integer userId) throws StoreException {
        return productData.isFavorite(productId, userId);
    }

    private void validateProduct(ProductDTO product) throws StoreException {
        if (product == null) {
            throw new StoreException("Product details are required.");
        }
        InputRules.requiredText(product.getName(), 255, "Product name");
        InputRules.requiredText(product.getDescription(), 255, "Product description");
        InputRules.requiredText(product.getImage(), 4000000, "Product image");
        InputRules.positiveId(product.getCategoryId(), "Category ID");
        Double price = product.getPrice();
        if (price == null || !Double.isFinite(price) || price < 0 || price > 999999999.99) {
            throw new StoreException("Price must be a finite, non-negative amount within the supported range.");
        }
    }

    private void validatePriceBound(Double value) throws StoreException {
        if (value != null && (!Double.isFinite(value) || value < 0)) {
            throw new StoreException("Price filters must be finite and non-negative.");
        }
    }
}
