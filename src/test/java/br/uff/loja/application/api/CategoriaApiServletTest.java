package br.uff.loja.application.api;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.uff.loja.core.interfaces.services.ICategoriaService;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;

public class CategoriaApiServletTest {
    private ICategoriaService categoryService;
    private CategoriaApiServlet servlet;
    private HttpServletResponse response;
    private StringWriter responseBody;

    @Before
    public void setUp() throws Exception {
        categoryService = mock(ICategoriaService.class);
        servlet = new CategoriaApiServlet(categoryService);
        response = mock(HttpServletResponse.class);
        responseBody = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
    }

    @Test
    public void hidesServiceErrorsFromClients() throws Exception {
        when(categoryService.listaCategorias()).thenThrow(new IllegalStateException("database secret"));

        servlet.doGet(mock(HttpServletRequest.class), response);

        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        assertEquals("{\"error\":\"The request could not be completed.\"}", responseBody.toString());
    }
}
