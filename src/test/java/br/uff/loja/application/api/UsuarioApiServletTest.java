package br.uff.loja.application.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import br.uff.loja.core.dtos.UsuarioDTO;
import br.uff.loja.core.interfaces.services.IUsuarioService;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;

public class UsuarioApiServletTest {
    private IUsuarioService userService;
    private UsuarioApiServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter responseBody;

    @Before
    public void setUp() throws Exception {
        userService = mock(IUsuarioService.class);
        servlet = new UsuarioApiServlet(userService);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        responseBody = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
    }

    @Test
    public void rejectsRequestsWithoutAnAuthenticatedSession() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertEquals("{\"error\":\"Authentication is required.\"}", responseBody.toString());
        verifyZeroInteractions(userService);
    }

    @Test
    public void preventsCustomersFromReadingAnotherAccount() throws Exception {
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(12);
        when(request.getPathInfo()).thenReturn("/13");
        when(userService.ehAdmin(12)).thenReturn(false);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(userService).ehAdmin(12);
        assertFalse(responseBody.toString().contains("password"));
    }

    @Test
    public void malformedUserIdsReturnAClientError() throws Exception {
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(12);
        when(request.getPathInfo()).thenReturn("/not-an-id");
        when(userService.ehAdmin(12)).thenReturn(false);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertEquals("{\"error\":\"A valid user ID is required.\"}", responseBody.toString());
    }

    @Test
    public void customersCanReadTheirAccountWithoutExposingPasswordData() throws Exception {
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(12);
        when(request.getPathInfo()).thenReturn("/12");
        when(userService.ehAdmin(12)).thenReturn(false);
        when(userService.encontraUsuarioPorId(12)).thenReturn(
                new UsuarioDTO(12, "Customer", "customer@example.test", "stored-hash", 2));

        servlet.doGet(request, response);

        assertEquals("{\"id\":12,\"nome\":\"Customer\",\"email\":\"customer@example.test\",\"permissaoId\":2}",
                responseBody.toString());
        assertFalse(responseBody.toString().contains("stored-hash"));
    }
}
