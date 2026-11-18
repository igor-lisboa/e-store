package br.uff.store.application.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.uff.store.core.interfaces.services.ICategoryService;
import br.uff.store.core.interfaces.services.IProductService;
import br.uff.store.core.interfaces.services.IUserService;
import br.uff.store.infrastructure.shared.Helper;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;

public class ProductAdminServletTest {
    private static final int ADMIN_ID = 1;
    private static final int CUSTOMER_ID = 22;
    private static final int PRODUCT_ID = 7;

    private IProductService productService;
    private IUserService userService;
    private ProductAdminServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher adminPage;

    @Before
    public void setUp() throws Exception {
        productService = mock(IProductService.class);
        userService = mock(IUserService.class);
        servlet = new ProductAdminServlet(productService, mock(ICategoryService.class), userService, new Helper());
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        adminPage = mock(RequestDispatcher.class);
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestDispatcher("pages/product-admin.jsp")).thenReturn(adminPage);
    }

    @Test
    public void guestCannotSaveProductAndIsSentToSignIn() throws Exception {
        when(session.getAttribute("userId")).thenReturn(null);
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("action")).thenReturn("save");
        when(request.getParameter("name")).thenReturn("Injected");
        when(request.getParameter("price")).thenReturn("1.00");

        servlet.doPost(request, response);

        verifyNoInteractions(productService);
        verify(session).setAttribute("msg", "User is not authorized.");
        verify(response).sendRedirect("account");
    }

    @Test
    public void customerCannotDeleteProductEvenWhenSelectedInSession() throws Exception {
        when(session.getAttribute("userId")).thenReturn(CUSTOMER_ID);
        when(userService.isAdmin(CUSTOMER_ID)).thenReturn(false);
        when(session.getAttribute("selectedId")).thenReturn(String.valueOf(PRODUCT_ID));
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("action")).thenReturn("delete");

        servlet.doPost(request, response);

        verify(productService, never()).deleteProductById(any());
        verify(session).setAttribute("msg", "User is not authorized.");
        verify(response).sendRedirect("account");
    }

    @Test
    public void customerCannotAddStockOrOpenTheProductEditor() throws Exception {
        when(session.getAttribute("userId")).thenReturn(CUSTOMER_ID);
        when(userService.isAdmin(CUSTOMER_ID)).thenReturn(false);
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("action")).thenReturn("addStock");
        when(request.getParameter("quantity")).thenReturn("50");
        when(session.getAttribute("selectedId")).thenReturn(String.valueOf(PRODUCT_ID));

        servlet.doPost(request, response);

        verify(productService, never()).addProductStock(any(), any());
        verify(productService, never()).findProductById(any());
        verify(response).sendRedirect("account");
    }

    @Test
    public void customerSelectingProductFromQueryDoesNotLoadItForEditing() throws Exception {
        when(session.getAttribute("userId")).thenReturn(CUSTOMER_ID);
        when(userService.isAdmin(CUSTOMER_ID)).thenReturn(false);
        when(request.getParameter("selectedId")).thenReturn(String.valueOf(PRODUCT_ID));

        servlet.doGet(request, response);

        verify(productService, never()).findProductById(eq(PRODUCT_ID));
        verify(session, never()).setAttribute(eq("selectedId"), any());
        verify(response).sendRedirect("account");
    }

    @Test
    public void adminDeleteThroughGetIsRejectedBeforeAnyChange() throws Exception {
        when(session.getAttribute("userId")).thenReturn(ADMIN_ID);
        when(userService.isAdmin(ADMIN_ID)).thenReturn(true);
        when(session.getAttribute("selectedId")).thenReturn(String.valueOf(PRODUCT_ID));
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("action")).thenReturn("delete");

        servlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        verify(productService, never()).deleteProductById(any());
    }

    @Test
    public void adminDeletesTheSelectedProductAndReportsSuccess() throws Exception {
        when(session.getAttribute("userId")).thenReturn(ADMIN_ID);
        when(userService.isAdmin(ADMIN_ID)).thenReturn(true);
        when(session.getAttribute("selectedId")).thenReturn(String.valueOf(PRODUCT_ID));
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("action")).thenReturn("delete");

        servlet.doPost(request, response);

        verify(productService).deleteProductById(PRODUCT_ID);
        verify(session).setAttribute("msg", "Product deleted.");
        verify(adminPage).forward(request, response);
    }

    @Test
    public void adminAddsStockToTheSelectedProductOnly() throws Exception {
        when(session.getAttribute("userId")).thenReturn(ADMIN_ID);
        when(userService.isAdmin(ADMIN_ID)).thenReturn(true);
        when(session.getAttribute("selectedId")).thenReturn(String.valueOf(PRODUCT_ID));
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("action")).thenReturn("addStock");
        when(request.getParameter("quantity")).thenReturn("3");

        servlet.doPost(request, response);

        verify(productService).addProductStock(PRODUCT_ID, 3);
        verify(session).setAttribute("msg", "Stock increased.");
    }
}
