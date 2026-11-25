package br.uff.store.application.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.uff.store.core.dtos.PaginationDTO;
import br.uff.store.core.dtos.SaleDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.services.IAddressService;
import br.uff.store.core.interfaces.services.ISaleService;
import br.uff.store.infrastructure.shared.Helper;
import java.util.Collections;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;

public class CheckoutServletTest {
    private static final int USER_ID = 22;

    private ISaleService saleService;
    private IAddressService addressService;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    @Before
    public void setUp() {
        saleService = mock(ISaleService.class);
        addressService = mock(IAddressService.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(USER_ID);
    }

    private CheckoutServlet servlet(boolean demoAllowed) {
        return new CheckoutServlet(saleService, addressService, new Helper(), demoAllowed);
    }

    @Test
    public void guestIsSentToSignInWithoutTouchingOrders() throws Exception {
        when(session.getAttribute("userId")).thenReturn(null);
        when(request.getMethod()).thenReturn("POST");

        servlet(true).doPost(request, response);

        verify(response).sendRedirect("account?redirect=cart");
        verifyNoInteractions(saleService);
    }

    @Test
    public void cancellationRequiresPostBecauseItChangesStock() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("action")).thenReturn("cancelOrder");
        when(request.getParameter("orderId")).thenReturn("7");

        servlet(true).doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        verify(saleService, never()).cancelSale(anyInt(), anyInt());
    }

    @Test
    public void cancellationUsesTheSessionUserNotAnyRequestParameter() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("action")).thenReturn("cancelOrder");
        when(request.getParameter("orderId")).thenReturn("7");
        when(request.getParameter("userId")).thenReturn("1");

        servlet(true).doPost(request, response);

        verify(saleService).cancelSale(USER_ID, 7);
        verify(response).sendRedirect("checkout?history");
    }

    @Test
    public void cancellationFailureIsShownAndReturnsToOrderHistory() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("action")).thenReturn("cancelOrder");
        when(request.getParameter("orderId")).thenReturn("7");
        doThrow(new StoreException("Unable to cancel the order.")).when(saleService).cancelSale(USER_ID, 7);

        servlet(true).doPost(request, response);

        verify(session).setAttribute("msg", "Unable to cancel the order.");
        verify(response).sendRedirect("checkout?history");
    }

    @Test
    public void malformedOrderIdIsRejectedByTheServiceInsteadOfCancellingAnything() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("action")).thenReturn("cancelOrder");
        when(request.getParameter("orderId")).thenReturn("not-a-number");
        doThrow(new StoreException("Order ID is required.")).when(saleService).cancelSale(USER_ID, null);

        servlet(true).doPost(request, response);

        verify(saleService).cancelSale(USER_ID, null);
        verify(session).setAttribute("msg", "Order ID is required.");
    }

    @Test
    public void demoCheckoutDisabledNeverRecordsASale() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("action")).thenReturn("paymentSuccess");
        when(session.getAttribute("cartId")).thenReturn(5);
        when(session.getAttribute("addressId")).thenReturn(9);

        servlet(false).doPost(request, response);

        verify(saleService, never()).recordSale(anyInt(), anyInt(), anyInt());
        verify(session).setAttribute("msg", "Demo checkout is disabled. Verified online payments are not configured.");
        verify(response).sendRedirect("cart");
    }

    @Test
    public void demoCheckoutEnabledRecordsTheSessionCartForTheSessionUser() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("action")).thenReturn("paymentSuccess");
        when(session.getAttribute("cartId")).thenReturn(5);
        when(session.getAttribute("addressId")).thenReturn(9);

        servlet(true).doPost(request, response);

        verify(saleService).recordSale(USER_ID, 5, 9);
        verify(response).sendRedirect("checkout?history");
    }

    @Test
    public void completionByGetIsRejectedEvenWhenDemoCheckoutIsEnabled() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("action")).thenReturn("paymentSuccess");

        servlet(true).doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        verify(saleService, never()).recordSale(anyInt(), anyInt(), anyInt());
    }

    @Test
    public void historyListsOrdersWithTheirDeliveryAddress() throws Exception {
        SaleDTO sale = new SaleDTO();
        sale.setId(3);
        sale.setAddressId(9);
        PaginationDTO<List<SaleDTO>> page = new PaginationDTO<>(1, Collections.singletonList(sale), 1);
        when(request.getParameter("history")).thenReturn("");
        when(saleService.listUserSales(USER_ID, 5, 1)).thenReturn(page);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);
        when(request.getRequestDispatcher("pages/orders.jsp")).thenReturn(dispatcher);

        servlet(true).doGet(request, response);

        verifyNoInteractions(addressService);
        verify(request).setAttribute("sales", page);
        verify(dispatcher).forward(request, response);
        assertEquals(1, page.getItems().size());
    }
}
