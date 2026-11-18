package br.uff.store.application.controllers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.uff.store.core.dtos.CartDTO;
import br.uff.store.core.interfaces.services.ICartService;
import br.uff.store.core.interfaces.services.IAddressService;
import br.uff.store.infrastructure.shared.Helper;
import java.util.Collections;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;

public class CartServletTest {
    private ICartService cartService;
    private CartServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    @Before
    public void setUp() throws Exception {
        cartService = mock(ICartService.class);
        servlet = new CartServlet(cartService, mock(IAddressService.class), new Helper());
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestDispatcher("pages/cart.jsp")).thenReturn(mock(RequestDispatcher.class));
        when(cartService.listaProductsCart(22)).thenReturn(Collections.emptyList());
        when(cartService.recuperaPrecoTotalDeUmCart(22)).thenReturn(0.0);
    }

    @Test
    public void ignoresLegacyClientControlledCartIdCookies() throws Exception {
        when(request.getCookies()).thenReturn(new Cookie[] {new Cookie("carrinhoId", "999")});
        CartDTO activeCart = new CartDTO();
        activeCart.setId(22);
        when(cartService.recuperaCartAtivo(null, null, "127.0.0.1")).thenReturn(activeCart);

        servlet.doGet(request, response);

        verify(cartService).recuperaCartAtivo(null, null, "127.0.0.1");
        verify(session).setAttribute("carrinhoId", 22);
    }

    @Test
    public void usesTheServerSideSessionCartReference() throws Exception {
        when(session.getAttribute("carrinhoId")).thenReturn("22");
        when(request.getCookies()).thenReturn(new Cookie[] {new Cookie("carrinhoId", "999")});
        CartDTO activeCart = new CartDTO();
        activeCart.setId(22);
        when(cartService.recuperaCartAtivo(22, null, "127.0.0.1")).thenReturn(activeCart);

        servlet.doGet(request, response);

        verify(cartService).recuperaCartAtivo(22, null, "127.0.0.1");
    }
}
