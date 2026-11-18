package br.uff.loja.application.controllers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.uff.loja.core.dtos.CarrinhoDTO;
import br.uff.loja.core.interfaces.services.ICarrinhoService;
import br.uff.loja.core.interfaces.services.IEnderecoService;
import br.uff.loja.infrastructure.shared.Helper;
import java.util.Collections;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;

public class CarrinhoServletTest {
    private ICarrinhoService cartService;
    private CarrinhoServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    @Before
    public void setUp() throws Exception {
        cartService = mock(ICarrinhoService.class);
        servlet = new CarrinhoServlet(cartService, mock(IEnderecoService.class), new Helper());
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestDispatcher("pages/carrinho.jsp")).thenReturn(mock(RequestDispatcher.class));
        when(cartService.listaProdutosCarrinho(22)).thenReturn(Collections.emptyList());
        when(cartService.recuperaPrecoTotalDeUmCarrinho(22)).thenReturn(0.0);
    }

    @Test
    public void ignoresLegacyClientControlledCartIdCookies() throws Exception {
        when(request.getCookies()).thenReturn(new Cookie[] {new Cookie("carrinhoId", "999")});
        CarrinhoDTO activeCart = new CarrinhoDTO();
        activeCart.setId(22);
        when(cartService.recuperaCarrinhoAtivo(null, null, "127.0.0.1")).thenReturn(activeCart);

        servlet.doGet(request, response);

        verify(cartService).recuperaCarrinhoAtivo(null, null, "127.0.0.1");
        verify(session).setAttribute("carrinhoId", 22);
    }

    @Test
    public void usesTheServerSideSessionCartReference() throws Exception {
        when(session.getAttribute("carrinhoId")).thenReturn("22");
        when(request.getCookies()).thenReturn(new Cookie[] {new Cookie("carrinhoId", "999")});
        CarrinhoDTO activeCart = new CarrinhoDTO();
        activeCart.setId(22);
        when(cartService.recuperaCarrinhoAtivo(22, null, "127.0.0.1")).thenReturn(activeCart);

        servlet.doGet(request, response);

        verify(cartService).recuperaCarrinhoAtivo(22, null, "127.0.0.1");
    }
}
