package br.uff.store.core.services;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import br.uff.store.core.dtos.ProductDTO;
import br.uff.store.core.dtos.ProductFilterDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IProductData;
import org.junit.Test;

public class ProductServiceTest {
    private final IProductData productData = mock(IProductData.class);
    private final ProductService service = new ProductService(productData);

    @Test
    public void existingFavoriteIsRemovedWithoutAddingAnother() throws Exception {
        when(productData.isFavorite(3, 9)).thenReturn(true);
        service.toggleFavorite(3, 9);
        verify(productData).removeFavorite(3, 9);
        verify(productData, never()).addFavorite(3, 9);
    }

    @Test
    public void missingFavoriteIsAddedWithoutRemovingAnother() throws Exception {
        when(productData.isFavorite(3, 9)).thenReturn(false);
        service.toggleFavorite(3, 9);
        verify(productData).addFavorite(3, 9);
        verify(productData, never()).removeFavorite(3, 9);
    }

    @Test
    public void invalidPricesNeverReachPersistence() throws Exception {
        for (Double price : new Double[] {null, -1.0, Double.NaN, Double.POSITIVE_INFINITY, 1000000000.0}) {
            ProductDTO product = validProduct();
            product.setPrice(price);
            try {
                service.insertProduct(product);
                fail("Invalid price was accepted: " + price);
            } catch (StoreException expected) {
                verifyZeroInteractions(productData);
            }
        }
    }

    @Test
    public void blankNamesAndInvalidCategoriesNeverReachPersistence() throws Exception {
        ProductDTO product = validProduct();
        product.setName("  ");
        assertInvalidProduct(product);
        product = validProduct();
        product.setCategoryId(0);
        assertInvalidProduct(product);
    }

    @Test
    public void validProductCanBeCreated() throws Exception {
        ProductDTO product = validProduct();
        service.insertProduct(product);
        verify(productData).insertProduct(product);
    }

    @Test
    public void reversedPriceBoundsNeverReachTheQueryAdapter() throws Exception {
        ProductFilterDTO filter = new ProductFilterDTO();
        filter.setPageSize(5);
        filter.setPageNumber(1);
        filter.setMinimumPrice(100.0);
        filter.setMaximumPrice(10.0);
        try {
            service.listStorefrontProducts(filter);
            fail("Reversed price bounds were accepted.");
        } catch (StoreException expected) {
            verifyZeroInteractions(productData);
        }
    }

    @Test
    public void anonymousUsersCannotMutateFavorites() throws Exception {
        try {
            service.toggleFavorite(3, null);
            fail("An anonymous favorite was accepted.");
        } catch (StoreException expected) {
            verifyZeroInteractions(productData);
        }
    }

    private ProductDTO validProduct() {
        return new ProductDTO(null, "Console", 299.99, "Game console", "img/ps4.jpg", 5, 0);
    }

    private void assertInvalidProduct(ProductDTO product) throws Exception {
        try {
            service.insertProduct(product);
            fail("Invalid product was accepted.");
        } catch (StoreException expected) {
            verifyZeroInteractions(productData);
        }
    }
}
