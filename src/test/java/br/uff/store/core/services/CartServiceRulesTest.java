package br.uff.store.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.uff.store.core.dtos.CartItemDTO;
import br.uff.store.core.dtos.ProductDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.ICartData;
import br.uff.store.core.interfaces.data.IProductData;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/** Rules that keep carts consistent: only open carts change, quantities stay within stock, and failures change nothing. */
public class CartServiceRulesTest {
    private ICartData cartData;
    private IProductData productData;
    private CartService service;

    @Before
    public void setUp() throws Exception {
        cartData = mock(ICartData.class);
        productData = mock(IProductData.class);
        service = new CartService(cartData, productData);
        when(cartData.cartExists(3)).thenReturn(true);
        when(cartData.isCartSold(3)).thenReturn(false);
        ProductDTO product = new ProductDTO();
        product.setQuantity(5);
        when(productData.findProductById(8)).thenReturn(product);
    }

    private interface Action {
        void run() throws Exception;
    }

    private void expectFailure(String expectedMessage, Action action) throws Exception {
        try {
            action.run();
            fail("Expected: " + expectedMessage);
        } catch (StoreException expected) {
            assertEquals(expectedMessage, expected.getMessage());
        }
    }

    @Test
    public void aProductNotYetInTheCartIsAddedWithoutUpdatingAQuantity() throws Exception {
        when(cartData.productQuantityInCart(3, 8)).thenReturn(0);

        service.addProductToCart(3, 8);

        verify(cartData).addProductToCart(3, 8);
        verify(cartData, never()).updateProductQuantity(anyInt(), anyInt(), anyInt());
    }

    @Test
    public void aProductNeverStockedCannotBeAddedEvenOnce() throws Exception {
        ProductDTO unavailable = new ProductDTO();
        unavailable.setQuantity(0);
        when(productData.findProductById(9)).thenReturn(unavailable);
        when(cartData.productQuantityInCart(3, 9)).thenReturn(0);

        expectFailure("Requested quantity exceeds available stock.", () -> service.addProductToCart(3, 9));

        verify(cartData, never()).addProductToCart(3, 9);
    }

    @Test
    public void aProductWithUnknownStockIsTreatedAsUnavailable() throws Exception {
        when(productData.findProductById(10)).thenReturn(new ProductDTO());
        when(cartData.productQuantityInCart(3, 10)).thenReturn(0);

        expectFailure("Requested quantity exceeds available stock.", () -> service.addProductToCart(3, 10));
    }

    @Test
    public void soldOrMissingCartsAcceptNoChanges() throws Exception {
        when(cartData.isCartSold(4)).thenReturn(true);
        when(cartData.cartExists(4)).thenReturn(true);
        when(cartData.cartExists(5)).thenReturn(false);
        when(cartData.isCartSold(5)).thenReturn(false);

        for (int cartId : new int[] {4, 5}) {
            final int cart = cartId;
            expectFailure("Unable to add product ID 8 in cart ID " + cart + ".", () -> service.addProductToCart(cart, 8));
            expectFailure("Unable to remove product ID 8 in cart ID " + cart + ".", () -> service.removeProductFromCart(cart, 8));
            expectFailure("Unable to update quantity for product ID 8 in cart ID " + cart + ".",
                    () -> service.changeProductQuantity(cart, 8, 2));
        }

        verify(cartData, never()).addProductToCart(anyInt(), anyInt());
        verify(cartData, never()).removeProductFromCart(anyInt(), anyInt());
        verify(cartData, never()).updateProductQuantity(anyInt(), anyInt(), anyInt());
    }

    @Test
    public void removingAProductThatIsNotInTheCartIsReportedAndChangesNothing() throws Exception {
        when(cartData.productQuantityInCart(3, 8)).thenReturn(0);

        expectFailure("Product ID 8 is not in cart ID 3.", () -> service.removeProductFromCart(3, 8));

        verify(cartData, never()).removeProductFromCart(anyInt(), anyInt());
    }

    @Test
    public void removingAProductThatIsInTheCartDeletesIt() throws Exception {
        when(cartData.productQuantityInCart(3, 8)).thenReturn(2);

        service.removeProductFromCart(3, 8);

        verify(cartData).removeProductFromCart(3, 8);
    }

    @Test
    public void changingTheQuantityOfAProductNotInTheCartIsRejected() throws Exception {
        when(cartData.containsProduct(3, 8)).thenReturn(false);

        expectFailure("The product is not in this cart.", () -> service.changeProductQuantity(3, 8, 2));

        verifyNoInteractions(productData);
    }

    @Test
    public void quantityChangesStayBetweenOneAndTheAvailableStock() throws Exception {
        when(cartData.containsProduct(3, 8)).thenReturn(true);

        service.changeProductQuantity(3, 8, 5);
        verify(cartData).updateProductQuantity(3, 8, 5);

        expectFailure("Requested quantity exceeds available stock.", () -> service.changeProductQuantity(3, 8, 6));
        expectFailure("Cart quantity must be greater than zero.", () -> service.changeProductQuantity(3, 8, -1));
        expectFailure("Cart quantity must be greater than zero.", () -> service.changeProductQuantity(3, 8, null));
        verify(cartData, never()).updateProductQuantity(3, 8, 6);
        verify(cartData, never()).updateProductQuantity(3, 8, -1);
    }

    @Test
    public void listingTheProductsOfAMissingCartIsAnError() throws Exception {
        when(cartData.cartExists(99)).thenReturn(false);

        expectFailure("Unable to list products in cart ID 99.", () -> service.listCartProducts(99));

        verify(cartData, never()).listCartProducts(99);
    }

    @Test
    public void listingAnExistingCartReturnsItsItems() throws Exception {
        List<CartItemDTO> items = Collections.singletonList(new CartItemDTO());
        when(cartData.listCartProducts(3)).thenReturn(items);

        assertEquals(items, service.listCartProducts(3));
    }

    @Test
    public void pagedListingRejectsBadPagingBeforeTouchingTheDatabase() throws Exception {
        expectFailure("Page size must be between 1 and 100 and page number must be positive.",
                () -> service.listCartProducts(3, 0, 1));
        expectFailure("Page size must be between 1 and 100 and page number must be positive.",
                () -> service.listCartProducts(3, 20, 0));

        verify(cartData, never()).listCartProducts(anyInt(), anyInt(), anyInt());
    }

    @Test
    public void aCartCanBeCheckedOutOnlyWhenItExistsBelongsToTheUserIsOpenAndHasItems() throws Exception {
        when(cartData.cartBelongsToUser(3, 7)).thenReturn(true);
        when(cartData.cartItemCount(3)).thenReturn(2);
        assertTrue(service.isValidActiveCart(3, 7));

        when(cartData.cartItemCount(3)).thenReturn(0);
        assertFalse("an empty cart", service.isValidActiveCart(3, 7));

        when(cartData.cartItemCount(3)).thenReturn(2);
        when(cartData.cartBelongsToUser(3, 8)).thenReturn(false);
        assertFalse("another user's cart", service.isValidActiveCart(3, 8));

        when(cartData.isCartSold(3)).thenReturn(true);
        assertFalse("an already sold cart", service.isValidActiveCart(3, 7));

        assertFalse("a missing cart", service.isValidActiveCart(99, 7));
    }

    @Test
    public void totalAndQuantityComeFromTheDataLayerUnchanged() throws Exception {
        when(cartData.getCartTotal(3)).thenReturn(new BigDecimal("59.90"));
        when(cartData.productQuantityInCart(3, 8)).thenReturn(4);

        assertEquals(new BigDecimal("59.90"), service.getCartTotal(3));
        assertEquals(Integer.valueOf(4), service.productQuantityInCart(3, 8));
    }

    @Test
    public void allDependenciesAreRequired() {
        try {
            new CartService(cartData, productData, null);
            fail("A clock is required.");
        } catch (IllegalArgumentException expected) {
            // Expected.
        }
        try {
            new CartService(null, productData);
            fail("Cart data is required.");
        } catch (IllegalArgumentException expected) {
            // Expected.
        }
    }
}
