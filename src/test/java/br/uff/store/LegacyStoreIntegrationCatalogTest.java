package br.uff.store;

import java.math.BigDecimal;

import br.uff.store.application.StoreServices;
import br.uff.store.core.dtos.CategoryDTO;
import br.uff.store.core.dtos.FeaturedProductDTO;
import br.uff.store.core.dtos.ProductDTO;
import br.uff.store.core.dtos.ProductFilterDTO;
import br.uff.store.core.dtos.ProductListDTO;
import br.uff.store.core.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;

public class LegacyStoreIntegrationCatalogTest extends StoreIntegrationFixture {
    @Test
    public void createdProductRetainsSubmittedDetails() throws Exception {
        ProductDTO product = createProduct("Integration console", new BigDecimal("12.50"), 7);
        ProductDTO saved = products.findProductById(product.getId());
        assertEquals("Integration console", saved.getName());
        assertEquals(new BigDecimal("12.50"), saved.getPrice());
        assertEquals("Integration fixture", saved.getDescription());
        assertEquals(Integer.valueOf(5), saved.getCategoryId());
        assertEquals(Integer.valueOf(7), saved.getQuantity());
    }

    @Test
    public void editChangesDetailsWithoutResettingStock() throws Exception {
        ProductDTO product = products.findProductById(1);
        product.setName("Updated console");
        product.setPrice(new BigDecimal("42.25"));
        products.updateProductById(1, product);
        ProductDTO saved = products.findProductById(1);
        assertEquals("Updated console", saved.getName());
        assertEquals(new BigDecimal("42.25"), saved.getPrice());
        assertEquals(Integer.valueOf(100), saved.getQuantity());
    }

    @Test
    public void deletingProductRemovesItFromLookupAndCatalog() throws Exception {
        products.deleteProductById(1);
        assertThrows(ResourceNotFoundException.class, () -> products.findProductById(1));
        assertEquals(2, products.listAdminProducts().size());
        assertFalse(products.listAdminProducts().stream().anyMatch(product -> product.getId() == 1));
    }

    @Test
    public void stockIncrementAddsToPersistedQuantity() throws Exception {
        products.addProductStock(1, 7);
        assertEquals(Integer.valueOf(107), products.findProductById(1).getQuantity());
        assertEquals(Integer.valueOf(100), products.findProductById(2).getQuantity());
    }

    @Test
    public void priceFilterIncludesBoundariesAndExcludesOtherPrices() throws Exception {
        ProductDTO lower = createProduct("Lower boundary", new BigDecimal("20.10"), 1);
        ProductDTO upper = createProduct("Upper boundary", new BigDecimal("28.80"), 1);
        createProduct("Below boundary", new BigDecimal("20.09"), 1);
        createProduct("Above boundary", new BigDecimal("28.81"), 1);
        ProductFilterDTO filter = new ProductFilterDTO();
        filter.setPageNumber(1);
        filter.setPageSize(10);
        filter.setMinimumPrice(new BigDecimal("20.10"));
        filter.setMaximumPrice(new BigDecimal("28.80"));
        List<Integer> ids = products.listStorefrontProducts(filter).getItems().stream()
                .map(ProductListDTO::getId).collect(Collectors.toList());
        assertEquals(2, ids.size());
        assertTrue(ids.contains(lower.getId()));
        assertTrue(ids.contains(upper.getId()));
    }

    @Test
    public void featuredProductsPreferStockAndRespectThreeItemLimit() throws Exception {
        ProductDTO featured = createProduct("High stock", new BigDecimal("10.00"), 500);
        ProductDTO unavailable = createProduct("No stock", new BigDecimal("10.00"), 0);
        List<FeaturedProductDTO> result = products.listFeaturedProducts();
        assertEquals(3, result.size());
        assertEquals(featured.getId(), result.get(0).getId());
        assertFalse(result.stream().anyMatch(product -> unavailable.getId().equals(product.getId())));
    }

    @Test
    public void storefrontListsOutOfStockProductsMarkedUnavailable() throws Exception {
        ProductDTO inStock = createProduct("Available item", new BigDecimal("10.00"), 3);
        ProductDTO soldOut = createProduct("Sold out item", new BigDecimal("10.00"), 0);
        ProductFilterDTO filter = new ProductFilterDTO();
        filter.setPageNumber(1);
        filter.setPageSize(100);
        Map<Integer, ProductListDTO> listed = products.listStorefrontProducts(filter).getItems().stream()
                .collect(Collectors.toMap(ProductListDTO::getId, product -> product));
        assertTrue(listed.get(inStock.getId()).isAvailable());
        assertFalse(listed.get(soldOut.getId()).isAvailable());
    }

    @Test
    public void referenceCategoriesRetainEnglishHierarchy() throws Exception {
        Map<Integer, String> categories = StoreServices.categories().categories().stream()
                .collect(Collectors.toMap(CategoryDTO::getId, CategoryDTO::getName));
        assertEquals(9, categories.size());
        assertEquals("Playstation > Consoles", categories.get(5));
        assertEquals("Xbox > Games", categories.get(9));
        assertEquals("Wii > Accessories", categories.get(10));
    }
}
