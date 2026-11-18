package br.uff.store.application.api;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import br.uff.store.core.interfaces.services.IProductService;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;

public class ProductApiServletTest {
    private IProductService productService;
    private ProductApiServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter responseBody;

    @Before
    public void setUp() throws Exception {
        productService = mock(IProductService.class);
        servlet = new ProductApiServlet(productService);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        responseBody = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
    }

    @Test
    public void rejectsMalformedProductIdsWithoutCallingTheService() throws Exception {
        when(request.getPathInfo()).thenReturn("/invalid");

        servlet.doGet(request, response);

        verifyZeroInteractions(productService);
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertEquals("{\"error\":\"A valid product ID is required.\"}", responseBody.toString());
    }

    @Test
    public void hidesServiceErrorsFromClients() throws Exception {
        when(request.getPathInfo()).thenReturn("/4");
        when(productService.encontraProductPorId(4)).thenThrow(new IllegalStateException("database secret"));

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        assertEquals("{\"error\":\"The request could not be completed.\"}", responseBody.toString());
    }
}
