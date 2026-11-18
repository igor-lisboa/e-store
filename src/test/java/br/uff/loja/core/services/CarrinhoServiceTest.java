package br.uff.loja.core.services;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.uff.loja.core.dtos.ProdutoDTO;
import br.uff.loja.core.exceptions.LojaException;
import br.uff.loja.core.interfaces.data.ICarrinhoData;
import br.uff.loja.core.interfaces.data.IProdutoData;
import org.junit.Before;
import org.junit.Test;

public class CarrinhoServiceTest {
    private ICarrinhoData cartData;
    private IProdutoData productData;
    private CarrinhoService service;

    @Before
    public void setUp() {
        cartData = mock(ICarrinhoData.class);
        productData = mock(IProdutoData.class);
        service = new CarrinhoService(cartData, productData);
    }

    @Test
    public void addingAnItemDoesNotExceedAvailableStock() throws Exception {
        when(cartData.carrinhoExiste(3)).thenReturn(true);
        when(cartData.carrinhoVendido(3)).thenReturn(false);
        when(cartData.quantidadeProdutoNoCarrinho(3, 8)).thenReturn(1);
        when(productData.encontraProdutoPorId(8)).thenReturn(productWithStock(2));

        service.insereProdutoCarrinho(3, 8);

        verify(cartData).atualizaQtdDoProdutoNoCarrinho(3, 8, 2);
    }

    @Test
    public void rejectsAddingItemsBeyondAvailableStock() throws Exception {
        when(cartData.carrinhoExiste(3)).thenReturn(true);
        when(cartData.carrinhoVendido(3)).thenReturn(false);
        when(cartData.quantidadeProdutoNoCarrinho(3, 8)).thenReturn(1);
        when(productData.encontraProdutoPorId(8)).thenReturn(productWithStock(1));

        try {
            service.insereProdutoCarrinho(3, 8);
            fail("Expected insufficient stock to be rejected.");
        } catch (LojaException expected) {
            verify(cartData, never()).atualizaQtdDoProdutoNoCarrinho(3, 8, 2);
            verify(cartData, never()).adicionaProdutoNoCarrinho(3, 8);
        }
    }

    @Test
    public void rejectsNonPositiveQuantitiesBeforePersistence() throws Exception {
        when(cartData.carrinhoExiste(3)).thenReturn(true);
        when(cartData.carrinhoVendido(3)).thenReturn(false);
        when(cartData.verificaProdutoNoCarrinho(3, 8)).thenReturn(true);

        try {
            service.alteraQuantidadeProdutoCarrinho(3, 8, 0);
            fail("Expected zero quantity to be rejected.");
        } catch (LojaException expected) {
            verify(productData, never()).encontraProdutoPorId(anyInt());
            verify(cartData, never()).atualizaQtdDoProdutoNoCarrinho(3, 8, 0);
        }
    }

    private ProdutoDTO productWithStock(Integer quantity) {
        ProdutoDTO product = new ProdutoDTO();
        product.setQuantidade(quantity);
        return product;
    }
}
