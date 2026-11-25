package br.uff.store.application.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.uff.store.core.dtos.PaginationDTO;
import br.uff.store.core.dtos.ProductFilterDTO;
import br.uff.store.core.dtos.ProductListDTO;
import br.uff.store.core.interfaces.services.IProductService;
import br.uff.store.infrastructure.shared.Helper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ProductsServletTest {
    private IProductService productService;
    private ProductsServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private final Map<String, Object> sessionState = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        productService = mock(IProductService.class);
        servlet = new ProductsServlet(productService, new Helper());
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(anyString())).thenAnswer(call -> sessionState.get(call.getArgument(0)));
        doAnswer(call -> sessionState.put(call.getArgument(0), call.getArgument(1))).when(session)
                .setAttribute(anyString(), any());
        when(request.getRequestDispatcher("pages/products.jsp")).thenReturn(mock(RequestDispatcher.class));
        when(productService.listStorefrontProducts(any())).thenAnswer(call -> {
            ProductFilterDTO filter = call.getArgument(0);
            return new PaginationDTO<List<ProductListDTO>>(filter.getPageNumber(), new ArrayList<ProductListDTO>(), 9);
        });
    }

    private ProductFilterDTO listedFilter() throws Exception {
        ArgumentCaptor<ProductFilterDTO> filter = ArgumentCaptor.forClass(ProductFilterDTO.class);
        verify(productService).listStorefrontProducts(filter.capture());
        return filter.getValue();
    }

    @Test
    public void missingOrUnparseablePageSizeUsesTheDefaultOfTwenty() {
        assertEquals(20, ProductsServlet.resolvePageSize(null));
        assertEquals(20, ProductsServlet.resolvePageSize(""));
        assertEquals(20, ProductsServlet.resolvePageSize("twenty"));
        assertEquals(20, ProductsServlet.resolvePageSize("2.5"));
    }

    @Test
    public void pageSizeBoundariesAreAcceptedInclusively() {
        assertEquals(1, ProductsServlet.resolvePageSize("1"));
        assertEquals(12, ProductsServlet.resolvePageSize("12"));
        assertEquals(100, ProductsServlet.resolvePageSize("100"));
    }

    @Test
    public void pageSizesOutsideOneToOneHundredFallBackToTheDefault() {
        assertEquals(20, ProductsServlet.resolvePageSize("0"));
        assertEquals(20, ProductsServlet.resolvePageSize("-5"));
        assertEquals(20, ProductsServlet.resolvePageSize("101"));
        assertEquals(20, ProductsServlet.resolvePageSize("2147483648"));
    }

    @Test
    public void guestAskingForFavoritesIsSentToSignInAndNothingIsListed() throws Exception {
        when(request.getParameterValues("special")).thenReturn(new String[] {"favorites"});

        servlet.doPost(request, response);

        verify(response).sendRedirect("account?redirect=products");
        verify(productService, never()).listStorefrontProducts(any());
        assertEquals("Sign in to view your favorites.", sessionState.get("msg"));
    }

    @Test
    public void signedInFavoritesFilterIsScopedToTheSessionUser() throws Exception {
        sessionState.put("userId", 22);
        when(request.getParameterValues("special")).thenReturn(new String[] {"favorites"});
        when(request.getParameter("userId")).thenReturn("99");

        servlet.doPost(request, response);

        ProductFilterDTO filter = listedFilter();
        assertTrue(filter.getFavoritesOnly());
        assertEquals(Integer.valueOf(22), filter.getUserId());
    }

    @Test
    public void searchAndPriceBoundsReachTheFilterAndUnparseablePricesAreIgnored() throws Exception {
        when(request.getParameter("search")).thenReturn("console");
        when(request.getParameter("price_min")).thenReturn("10.50");
        when(request.getParameter("price_max")).thenReturn("not-a-price");

        servlet.doPost(request, response);

        ProductFilterDTO filter = listedFilter();
        assertEquals("console", filter.getSearch());
        assertEquals(new BigDecimal("10.50"), filter.getMinimumPrice());
        assertNull(filter.getMaximumPrice());
    }

    @Test
    public void categoryParameterSelectsThePlatformAndTypeInTheFilter() throws Exception {
        when(request.getParameter("categoryId")).thenReturn("5");

        servlet.doGet(request, response);

        ProductFilterDTO filter = listedFilter();
        assertEquals(java.util.Collections.singletonList("playstation"), filter.getCategories());
        assertEquals(java.util.Collections.singletonList("consoles"), filter.getSubcategories());
    }

    @Test
    public void requestedPageSizeIsHonoredWithinBounds() throws Exception {
        when(request.getParameter("pageSize")).thenReturn("50");

        servlet.doGet(request, response);

        assertEquals(Integer.valueOf(50), listedFilter().getPageSize());
    }

    @Test
    public void nextMovesOneAheadOfTheRememberedPageAndRemembersTheResult() throws Exception {
        sessionState.put("productPage", 3);
        when(request.getParameter("action")).thenReturn("next");

        servlet.doPost(request, response);

        assertEquals(Integer.valueOf(4), listedFilter().getPageNumber());
        assertEquals(4, sessionState.get("productPage"));
        assertEquals(9, sessionState.get("maxPage"));
    }

    @Test
    public void previousFromThePageOneNeverRequestsAPageBelowOne() throws Exception {
        sessionState.put("productPage", 1);
        when(request.getParameter("action")).thenReturn("previous");

        servlet.doPost(request, response);

        assertEquals(Integer.valueOf(1), listedFilter().getPageNumber());
    }

    @Test
    public void unexpectedFailuresDoNotLeakInternalDetails() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalStateException("SQL secret")).when(productService)
                .listStorefrontProducts(any());

        servlet.doGet(request, response);

        assertEquals("The request could not be completed.", sessionState.get("msg"));
        verify(response).sendRedirect("");
    }
}
