package br.uff.store.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.uff.store.core.dtos.ProductDTO;
import br.uff.store.core.interfaces.data.IProductData;
import org.junit.Test;
import org.mockito.Mockito;

public class ProductServiceTest {
    private final IProductData productData = Mockito.mock(IProductData.class);
    private final ProductService productService = new ProductService(productData);

    @Test
    public void delegatesProductLookup() throws Exception {
        ProductDTO product = new ProductDTO();
        when(productData.encontraProductPorId(7)).thenReturn(product);

        assertEquals(product, productService.encontraProductPorId(7));
        verify(productData).encontraProductPorId(7);
    }

    @Test
    public void togglesFavoriteBasedOnCurrentState() throws Exception {
        when(productData.produtoFavoritadoPeloUser(3, 9)).thenReturn(true, false);

        productService.usuarioToogleFavoritaProductPorId(3, 9);
        productService.usuarioToogleFavoritaProductPorId(3, 9);

        verify(productData).removeFavoritacaoProductPeloUser(3, 9);
        verify(productData).adicionaFavoritacaoProductPeloUser(3, 9);
    }

    @Test
    public void exposesFavoriteStateFromDataSource() throws Exception {
        when(productData.produtoFavoritadoPeloUser(3, 9)).thenReturn(false);

        assertFalse(productService.produtoFavoritadoPeloUser(3, 9));
        verify(productData).produtoFavoritadoPeloUser(3, 9);
    }
}
