package br.uff.loja.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.uff.loja.core.dtos.ProdutoDTO;
import br.uff.loja.core.interfaces.data.IProdutoData;
import org.junit.Test;
import org.mockito.Mockito;

public class ProdutoServiceTest {
    private final IProdutoData productData = Mockito.mock(IProdutoData.class);
    private final ProdutoService productService = new ProdutoService(productData);

    @Test
    public void delegatesProductLookup() throws Exception {
        ProdutoDTO product = new ProdutoDTO();
        when(productData.encontraProdutoPorId(7)).thenReturn(product);

        assertEquals(product, productService.encontraProdutoPorId(7));
        verify(productData).encontraProdutoPorId(7);
    }

    @Test
    public void togglesFavoriteBasedOnCurrentState() throws Exception {
        when(productData.produtoFavoritadoPeloUsuario(3, 9)).thenReturn(true, false);

        productService.usuarioToogleFavoritaProdutoPorId(3, 9);
        productService.usuarioToogleFavoritaProdutoPorId(3, 9);

        verify(productData).removeFavoritacaoProdutoPeloUsuario(3, 9);
        verify(productData).adicionaFavoritacaoProdutoPeloUsuario(3, 9);
    }

    @Test
    public void exposesFavoriteStateFromDataSource() throws Exception {
        when(productData.produtoFavoritadoPeloUsuario(3, 9)).thenReturn(false);

        assertFalse(productService.produtoFavoritadoPeloUsuario(3, 9));
        verify(productData).produtoFavoritadoPeloUsuario(3, 9);
    }
}
