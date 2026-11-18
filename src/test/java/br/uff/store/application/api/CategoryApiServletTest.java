package br.uff.store.application.api;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.uff.store.core.interfaces.services.ICategoryService;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;

public class CategoryApiServletTest {
    private ICategoryService categoryService;
    private CategoryApiServlet servlet;
    private HttpServletResponse response;
    private StringWriter responseBody;

    @Before
    public void setUp() throws Exception {
        categoryService = mock(ICategoryService.class);
        servlet = new CategoryApiServlet(categoryService);
        response = mock(HttpServletResponse.class);
        responseBody = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
    }

    @Test
    public void hidesServiceErrorsFromClients() throws Exception {
        when(categoryService.listaCategorys()).thenThrow(new IllegalStateException("database secret"));

        servlet.doGet(mock(HttpServletRequest.class), response);

        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        assertEquals("{\"error\":\"The request could not be completed.\"}", responseBody.toString());
    }
}
