package br.uff.store.application.api;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import br.uff.store.core.dtos.ProductDTO;
import br.uff.store.core.exceptions.ResourceNotFoundException;
import br.uff.store.core.interfaces.services.IProductService;
import br.uff.store.core.interfaces.services.IUserService;
import br.uff.store.infrastructure.shared.Helper;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;

public class ProductApiServletTest {
    private static final int ADMIN_ID = 1;
    private static final int CUSTOMER_ID = 22;

    private IProductService productService;
    private IUserService userService;
    private ProductApiServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private StringWriter responseBody;

    @Before
    public void setUp() throws Exception {
        productService = mock(IProductService.class);
        userService = mock(IUserService.class);
        servlet = new ProductApiServlet(productService, userService, new Helper());
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        responseBody = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
        when(request.getSession(false)).thenReturn(session);
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
        when(productService.findProductById(4)).thenThrow(new IllegalStateException("database secret"));

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        assertEquals("{\"error\":\"The request could not be completed.\"}", responseBody.toString());
    }

    @Test
    public void missingProductsReturnNotFoundRatherThanServerError() throws Exception {
        when(request.getPathInfo()).thenReturn("/99");
        when(productService.findProductById(99)).thenThrow(new ResourceNotFoundException("Product"));
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        assertEquals("{\"error\":\"Product was not found.\"}", responseBody.toString());
    }

    @Test
    public void singleProductLookupRemainsAvailableWithoutSignIn() throws Exception {
        when(request.getPathInfo()).thenReturn("/4");
        when(request.getSession(false)).thenReturn(null);
        when(productService.findProductById(4))
                .thenReturn(new ProductDTO(4, "Public Product", new BigDecimal("9.50"), "Description", "img.png", 1, 3));

        servlet.doGet(request, response);

        verify(productService).findProductById(4);
        assertTrue(responseBody.toString().contains("\"name\":\"Public Product\""));
    }

    @Test
    public void fullProductListRequiresSignIn() throws Exception {
        when(request.getPathInfo()).thenReturn("/");
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(productService, never()).listAdminProducts();
        assertEquals("{\"error\":\"Authentication is required.\"}", responseBody.toString());
    }

    @Test
    public void customerCannotListStockAndDescriptionsForEveryProduct() throws Exception {
        when(request.getPathInfo()).thenReturn("/");
        when(session.getAttribute("userId")).thenReturn(CUSTOMER_ID);
        when(userService.isAdmin(CUSTOMER_ID)).thenReturn(false);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(productService, never()).listAdminProducts();
        assertEquals("{\"error\":\"Administrator access is required.\"}", responseBody.toString());
    }

    @Test
    public void administratorReceivesTheFullProductListWithStockQuantities() throws Exception {
        when(request.getPathInfo()).thenReturn("/");
        when(session.getAttribute("userId")).thenReturn(ADMIN_ID);
        when(userService.isAdmin(ADMIN_ID)).thenReturn(true);
        when(productService.listAdminProducts()).thenReturn(Collections.singletonList(
                new ProductDTO(4, "Hidden Stock Product", new BigDecimal("9.50"), "Description", "img.png", 1, 3)));

        servlet.doGet(request, response);

        verify(response, never()).setStatus(org.mockito.ArgumentMatchers.anyInt());
        assertTrue(responseBody.toString().contains("\"name\":\"Hidden Stock Product\""));
        assertTrue(responseBody.toString().contains("\"quantity\":3"));
    }

    @Test
    public void administratorListingFailureIsHiddenFromClients() throws Exception {
        when(request.getPathInfo()).thenReturn("/");
        when(session.getAttribute("userId")).thenReturn(ADMIN_ID);
        when(userService.isAdmin(ADMIN_ID)).thenReturn(true);
        when(productService.listAdminProducts()).thenThrow(new IllegalStateException("database secret"));

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        assertEquals("{\"error\":\"The request could not be completed.\"}", responseBody.toString());
    }
}
