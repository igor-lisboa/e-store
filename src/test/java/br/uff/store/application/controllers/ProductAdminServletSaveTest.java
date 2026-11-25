package br.uff.store.application.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.uff.store.core.dtos.ProductDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.services.ICategoryService;
import br.uff.store.core.interfaces.services.IProductService;
import br.uff.store.core.interfaces.services.IUserService;
import br.uff.store.infrastructure.shared.Helper;
import java.math.BigDecimal;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/** Administrator saves: what is sent to the service, which method is allowed, and what the page says on failure. */
public class ProductAdminServletSaveTest {
    private IProductService productService;
    private ProductAdminServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    @Before
    public void setUp() throws Exception {
        productService = mock(IProductService.class);
        IUserService userService = mock(IUserService.class);
        servlet = new ProductAdminServlet(productService, mock(ICategoryService.class), userService, new Helper());
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestDispatcher("pages/product-admin.jsp")).thenReturn(mock(RequestDispatcher.class));
        when(session.getAttribute("userId")).thenReturn(1);
        when(userService.isAdmin(1)).thenReturn(true);
        when(request.getParameter("action")).thenReturn("save");
        when(request.getParameter("name")).thenReturn("Pad");
        when(request.getParameter("price")).thenReturn("19.90");
        when(request.getParameter("description")).thenReturn("A pad");
        when(request.getParameter("src")).thenReturn("img/pad.png");
        when(request.getParameter("categoryId")).thenReturn("4");
    }

    @Test
    public void savingWithoutASelectionCreatesANewProductFromTheFormFields() throws Exception {
        servlet.doPost(request, response);

        ArgumentCaptor<ProductDTO> created = ArgumentCaptor.forClass(ProductDTO.class);
        verify(productService).insertProduct(created.capture());
        assertEquals("Pad", created.getValue().getName());
        assertEquals(new BigDecimal("19.90"), created.getValue().getPrice());
        assertEquals("img/pad.png", created.getValue().getImage());
        assertEquals(Integer.valueOf(4), created.getValue().getCategoryId());
        assertNull("a new product has no id before it is stored", created.getValue().getId());
        verify(session).setAttribute("msg", "Product saved.");
    }

    @Test
    public void savingWithASelectionUpdatesOnlyThatProduct() throws Exception {
        when(session.getAttribute("selectedId")).thenReturn("7");

        servlet.doPost(request, response);

        verify(productService).updateProductById(org.mockito.ArgumentMatchers.eq(7), any(ProductDTO.class));
        verify(productService, never()).insertProduct(any());
    }

    @Test
    public void anUnreadablePriceIsPassedOnAsMissingSoTheServiceRejectsIt() throws Exception {
        when(request.getParameter("price")).thenReturn("cheap");
        doThrow(new StoreException("Price must be a positive amount.")).when(productService).insertProduct(any());

        servlet.doPost(request, response);

        ArgumentCaptor<ProductDTO> attempted = ArgumentCaptor.forClass(ProductDTO.class);
        verify(productService).insertProduct(attempted.capture());
        assertNull(attempted.getValue().getPrice());
        verify(session).setAttribute("msg", "Price must be a positive amount.");
        verify(session, never()).setAttribute("msg", "Product saved.");
    }

    @Test
    public void savingByGetIsRefusedAndStoresNothing() throws Exception {
        when(request.getMethod()).thenReturn("GET");

        servlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        verify(productService, never()).insertProduct(any());
        verify(productService, never()).updateProductById(anyInt(), any());
    }

    @Test
    public void clearingForgetsTheSelectedProduct() throws Exception {
        when(request.getParameter("action")).thenReturn("clear");

        servlet.doPost(request, response);

        verify(session).setAttribute("selectedId", null);
        verify(productService, never()).insertProduct(any());
    }

    @Test
    public void anUnexpectedFailureNeverShowsInternalDetails() throws Exception {
        doThrow(new IllegalStateException("jdbc:mysql://db:3306/e_store refused")).when(productService).insertProduct(any());

        servlet.doPost(request, response);

        verify(session).setAttribute("msg", "The request could not be completed.");
    }
}
