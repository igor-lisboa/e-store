package br.uff.store.core.services;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.uff.store.core.dtos.ProductDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.ICartData;
import br.uff.store.core.interfaces.data.IProductData;
import org.junit.Before;
import org.junit.Test;

public class CartServiceTest {
    private ICartData cartData;
    private IProductData productData;
    private CartService service;

    @Before
    public void setUp() {
        cartData = mock(ICartData.class);
        productData = mock(IProductData.class);
        service = new CartService(cartData, productData);
    }

    @Test
    public void addingAnItemDoesNotExceedAvailableStock() throws Exception {
        when(cartData.carrinhoExiste(3)).thenReturn(true);
        when(cartData.carrinhoVendido(3)).thenReturn(false);
        when(cartData.quantidadeProductNoCart(3, 8)).thenReturn(1);
        when(productData.encontraProductPorId(8)).thenReturn(productWithStock(2));

        service.insereProductCart(3, 8);

        verify(cartData).atualizaQtdDoProductNoCart(3, 8, 2);
    }

    @Test
    public void rejectsAddingItemsBeyondAvailableStock() throws Exception {
        when(cartData.carrinhoExiste(3)).thenReturn(true);
        when(cartData.carrinhoVendido(3)).thenReturn(false);
        when(cartData.quantidadeProductNoCart(3, 8)).thenReturn(1);
        when(productData.encontraProductPorId(8)).thenReturn(productWithStock(1));

        try {
            service.insereProductCart(3, 8);
            fail("Expected insufficient stock to be rejected.");
        } catch (StoreException expected) {
            verify(cartData, never()).atualizaQtdDoProductNoCart(3, 8, 2);
            verify(cartData, never()).adicionaProductNoCart(3, 8);
        }
    }

    @Test
    public void rejectsNonPositiveQuantitiesBeforePersistence() throws Exception {
        when(cartData.carrinhoExiste(3)).thenReturn(true);
        when(cartData.carrinhoVendido(3)).thenReturn(false);
        when(cartData.verificaProductNoCart(3, 8)).thenReturn(true);

        try {
            service.alteraQuantidadeProductCart(3, 8, 0);
            fail("Expected zero quantity to be rejected.");
        } catch (StoreException expected) {
            verify(productData, never()).encontraProductPorId(anyInt());
            verify(cartData, never()).atualizaQtdDoProductNoCart(3, 8, 0);
        }
    }

    private ProductDTO productWithStock(Integer quantity) {
        ProductDTO product = new ProductDTO();
        product.setQuantidade(quantity);
        return product;
    }
}
