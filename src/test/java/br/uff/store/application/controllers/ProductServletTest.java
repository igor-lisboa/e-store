package br.uff.store.application.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.uff.store.core.dtos.CustomerProductDTO;
import br.uff.store.core.interfaces.services.IProductService;
import br.uff.store.core.interfaces.services.IReviewService;
import br.uff.store.infrastructure.shared.Helper;
import java.util.Collections;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;

public class ProductServletTest {
    private IProductService productService;
    private IReviewService reviewService;
    private ProductServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    @Before
    public void setUp() {
        productService = mock(IProductService.class);
        reviewService = mock(IReviewService.class);
        servlet = new ProductServlet(productService, reviewService, new Helper());
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(request.getMethod()).thenReturn("GET");
    }

    @Test
    public void withoutASelectedProductTheVisitorIsSentBackToTheCatalog() throws Exception {
        servlet.doGet(request, response);

        verify(session).setAttribute("msg", "Please select a product.");
        verify(response).sendRedirect("products");
        verify(productService, never()).getCustomerProduct(any(), any());
    }

    @Test
    public void guestSeesTheProductWithoutUserSpecificData() throws Exception {
        when(request.getParameter("productId")).thenReturn("7");
        when(session.getAttribute("productId")).thenReturn(7);
        CustomerProductDTO product = new CustomerProductDTO();
        when(productService.getCustomerProduct(7, null)).thenReturn(product);
        when(reviewService.listProductReviews(7)).thenReturn(Collections.emptyList());
        RequestDispatcher page = mock(RequestDispatcher.class);
        when(request.getRequestDispatcher("pages/product.jsp")).thenReturn(page);

        servlet.doGet(request, response);

        verify(request).setAttribute("product", product);
        verify(page).forward(request, response);
    }

    @Test
    public void signedInVisitorGetsTheirOwnFavoriteAndRatingState() throws Exception {
        when(session.getAttribute("productId")).thenReturn(7);
        when(session.getAttribute("userId")).thenReturn(22);
        when(productService.getCustomerProduct(7, 22)).thenReturn(new CustomerProductDTO());
        when(request.getRequestDispatcher("pages/product.jsp")).thenReturn(mock(RequestDispatcher.class));

        servlet.doGet(request, response);

        verify(productService).getCustomerProduct(7, 22);
    }

    @Test
    public void togglingAFavoriteByGetIsRejected() throws Exception {
        when(session.getAttribute("productId")).thenReturn(7);
        when(session.getAttribute("userId")).thenReturn(22);
        when(request.getParameter("fav")).thenReturn("7");

        servlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        verify(productService, never()).toggleFavorite(anyInt(), anyInt());
    }

    @Test
    public void guestCannotFavoriteAndIsSentToSignIn() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(session.getAttribute("productId")).thenReturn(7);
        when(request.getParameter("fav")).thenReturn("7");

        servlet.doPost(request, response);

        verify(response).sendRedirect("account?redirect=product");
        verify(productService, never()).toggleFavorite(anyInt(), anyInt());
    }

    @Test
    public void signedInVisitorTogglesTheirOwnFavoriteOnly() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(session.getAttribute("productId")).thenReturn(7);
        when(session.getAttribute("userId")).thenReturn(22);
        when(request.getParameter("fav")).thenReturn("7");
        when(request.getParameter("userId")).thenReturn("99");

        servlet.doPost(request, response);

        verify(productService).toggleFavorite(7, 22);
        verify(response).sendRedirect("product");
    }

    @Test
    public void unexpectedFailuresDoNotLeakInternalDetails() throws Exception {
        when(session.getAttribute("productId")).thenReturn(7);
        when(productService.getCustomerProduct(7, null)).thenThrow(new IllegalStateException("SQL: select * from secrets"));

        servlet.doGet(request, response);

        verify(session).setAttribute("msg", "The request could not be completed.");
        verify(response).sendRedirect("products");
    }
}
