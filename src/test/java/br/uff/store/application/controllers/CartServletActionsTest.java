package br.uff.store.application.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.uff.store.core.dtos.CartDTO;
import br.uff.store.core.interfaces.services.IAddressService;
import br.uff.store.core.interfaces.services.ICartService;
import br.uff.store.infrastructure.shared.Helper;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collections;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;

/** Which cart actions need POST, which identity they use, and what a visitor sees when something goes wrong. */
public class CartServletActionsTest {
    private ICartService cartService;
    private IAddressService addressService;
    private CartServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher cartPage;
    private RequestDispatcher checkoutPage;

    @Before
    public void setUp() throws Exception {
        cartService = mock(ICartService.class);
        addressService = mock(IAddressService.class);
        servlet = new CartServlet(cartService, addressService, new Helper());
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        cartPage = mock(RequestDispatcher.class);
        checkoutPage = mock(RequestDispatcher.class);
        when(request.getSession()).thenReturn(session);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getContextPath()).thenReturn("");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestDispatcher("pages/cart.jsp")).thenReturn(cartPage);
        when(request.getRequestDispatcher("checkout")).thenReturn(checkoutPage);
        when(request.getRequestDispatcher("pages/cart-confirmation.jsp")).thenReturn(mock(RequestDispatcher.class));
        when(session.getAttribute("userId")).thenReturn(5);
        CartDTO cart = new CartDTO();
        cart.setId(22);
        when(cartService.getActiveCart(any(), any(), any())).thenReturn(cart);
        when(cartService.listCartProducts(22)).thenReturn(Collections.emptyList());
        when(cartService.getCartTotal(22)).thenReturn(BigDecimal.ZERO);
    }

    private void parameter(String name, String value) {
        when(request.getParameter(name)).thenReturn(value);
    }

    @Test
    public void theSignedInUserComesFromTheSessionNeverFromARequestParameter() throws Exception {
        parameter("userId", "1");

        servlet.doPost(request, response);

        verify(cartService).getActiveCart(null, 5, "127.0.0.1");
    }

    @Test
    public void addingAProductByGetIsRefusedAndChangesNothing() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        parameter("addProductId", "8");

        servlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        verify(cartService, never()).addProductToCart(anyInt(), anyInt());
    }

    @Test
    public void addingAProductByPostAddsItAndShowsTheCart() throws Exception {
        parameter("addProductId", "8");

        servlet.doPost(request, response);

        verify(cartService).addProductToCart(22, 8);
        verify(session).setAttribute("msg", "Product added to the cart.");
        verify(cartPage).forward(request, response);
    }

    @Test
    public void aNonNumericProductIdAddsNothing() throws Exception {
        parameter("addProductId", "eight");

        servlet.doPost(request, response);

        verify(cartService, never()).addProductToCart(anyInt(), anyInt());
    }

    @Test
    public void removingOrChangingByGetIsRefusedAndChangesNothing() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        parameter("productId", "8");
        for (String action : new String[] {"removeProduct", "changeQuantity"}) {
            parameter("action", action);
            servlet.doGet(request, response);
        }

        verify(cartService, never()).removeProductFromCart(anyInt(), anyInt());
        verify(cartService, never()).changeProductQuantity(anyInt(), anyInt(), any());
    }

    @Test
    public void removingByPostRemovesTheProductFromTheSessionCart() throws Exception {
        parameter("action", "removeProduct");
        parameter("productId", "8");

        servlet.doPost(request, response);

        verify(cartService).removeProductFromCart(22, 8);
        verify(cartPage).forward(request, response);
    }

    @Test
    public void changingTheQuantityPassesAMissingOrUnreadableQuantityOnAsNullSoTheServiceRejectsIt() throws Exception {
        parameter("action", "changeQuantity");
        parameter("productId", "8");
        parameter("quantity", "3");
        servlet.doPost(request, response);
        verify(cartService).changeProductQuantity(22, 8, 3);

        parameter("quantity", "many");
        servlet.doPost(request, response);
        parameter("quantity", null);
        servlet.doPost(request, response);

        verify(cartService, org.mockito.Mockito.times(2)).changeProductQuantity(22, 8, null);
    }

    @Test
    public void continuingToPaymentWithoutAnAddressSendsTheVisitorBackToTheCart() throws Exception {
        parameter("action", "continuePayment");

        servlet.doPost(request, response);

        verify(session).setAttribute("msg", "Select an address.");
        verify(session).setAttribute("addressId", null);
        verify(response).sendRedirect("cart");
        verify(checkoutPage, never()).forward(any(), any());
    }

    @Test
    public void continuingToPaymentRemembersTheChosenAddressAndForwardsToCheckout() throws Exception {
        parameter("action", "continuePayment");
        parameter("addressId", "14");

        servlet.doPost(request, response);

        verify(session).setAttribute("addressId", 14);
        verify(checkoutPage).forward(request, response);
    }

    @Test
    public void confirmingTheCartLoadsOnlyTheSignedInUsersAddresses() throws Exception {
        parameter("action", "confirmCart");

        servlet.doPost(request, response);

        verify(addressService).listUserAddresses(5);
        verify(session).setAttribute("totalPrice", BigDecimal.ZERO);
    }

    @Test
    public void continueShoppingReturnsToTheCatalog() throws Exception {
        parameter("action", "continueShopping");

        servlet.doPost(request, response);

        verify(response).sendRedirect("products");
    }

    @Test
    public void aFailureShowsASafeMessageAndReturnsToTheCatalog() throws Exception {
        parameter("action", "removeProduct");
        parameter("productId", "8");
        doThrow(new IllegalStateException("jdbc:mysql://db refused", new SQLException())).when(cartService).removeProductFromCart(22, 8);

        servlet.doPost(request, response);

        verify(session).setAttribute("msg", "The request could not be completed.");
        verify(response).sendRedirect("products");
    }
}
