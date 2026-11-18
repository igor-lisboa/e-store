package br.uff.store.application.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.uff.store.core.dtos.AddressDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IAddressData;
import br.uff.store.core.interfaces.services.IAddressService;
import br.uff.store.core.services.AddressService;
import br.uff.store.infrastructure.shared.Helper;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class AddressServletTest {
    private static final int OWNER_ID = 22;
    private static final int OTHER_USER_ID = 99;
    private static final int OWNED_ADDRESS_ID = 5;
    private static final int FOREIGN_ADDRESS_ID = 6;

    private IAddressService addressService;
    private AddressServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher addressesPage;
    private RequestDispatcher editPage;

    @Before
    public void setUp() throws Exception {
        addressService = mock(IAddressService.class);
        servlet = new AddressServlet(addressService, new Helper());
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        addressesPage = mock(RequestDispatcher.class);
        editPage = mock(RequestDispatcher.class);
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestDispatcher("pages/addresses.jsp")).thenReturn(addressesPage);
        when(request.getRequestDispatcher("pages/address-edit.jsp")).thenReturn(editPage);
        when(session.getAttribute("userId")).thenReturn(OWNER_ID);
    }

    @Test
    public void guestIsRedirectedToSignInWithoutTouchingAddresses() throws Exception {
        when(session.getAttribute("userId")).thenReturn(null);
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("action")).thenReturn("delete");
        when(session.getAttribute("selectedId")).thenReturn(String.valueOf(OWNED_ADDRESS_ID));

        servlet.doPost(request, response);

        verify(response).sendRedirect("account?redirect=addresses");
        verifyNoInteractions(addressService);
    }

    @Test
    public void selectingAnotherAccountsAddressIsDeniedAndNotRememberedInSession() throws Exception {
        AddressDTO foreign = new AddressDTO("Office", OTHER_USER_ID, 20000, "Rua A", "Rio", "RJ", "Brasil");
        foreign.setId(FOREIGN_ADDRESS_ID);
        when(request.getParameter("selectedId")).thenReturn(String.valueOf(FOREIGN_ADDRESS_ID));
        when(addressService.findAddressById(FOREIGN_ADDRESS_ID)).thenReturn(foreign);
        when(addressService.addressBelongsToUser(FOREIGN_ADDRESS_ID, OWNER_ID)).thenReturn(false);

        servlet.doGet(request, response);

        verify(session).setAttribute("msg", "The address does not belong to this account.");
        verify(session, never()).setAttribute(eq("selectedId"), any());
        verify(response).sendRedirect("account");
        verify(editPage, never()).forward(request, response);
    }

    @Test
    public void foreignAddressDeleteIsBlockedAtTheDataBoundary() throws Exception {
        IAddressData addressData = mock(IAddressData.class);
        when(addressData.addressBelongsToUser(FOREIGN_ADDRESS_ID, OWNER_ID)).thenReturn(false);
        AddressServlet guardedServlet = new AddressServlet(new AddressService(addressData), new Helper());
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("action")).thenReturn("delete");
        when(session.getAttribute("selectedId")).thenReturn(String.valueOf(FOREIGN_ADDRESS_ID));

        guardedServlet.doPost(request, response);

        verify(addressData, never()).deleteAddressById(any());
        verify(session).setAttribute("msg", "The address does not belong to this account.");
        verify(response).sendRedirect("account");
        verify(addressesPage, never()).forward(request, response);
    }

    @Test
    public void foreignAddressUpdateIsBlockedAtTheDataBoundary() throws Exception {
        IAddressData addressData = mock(IAddressData.class);
        when(addressData.addressBelongsToUser(FOREIGN_ADDRESS_ID, OWNER_ID)).thenReturn(false);
        AddressServlet guardedServlet = new AddressServlet(new AddressService(addressData), new Helper());
        stubSaveParameters();
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("action")).thenReturn("save");
        when(session.getAttribute("selectedId")).thenReturn(String.valueOf(FOREIGN_ADDRESS_ID));

        guardedServlet.doPost(request, response);

        verify(addressData, never()).updateAddressById(any(), any());
        verify(session).setAttribute("msg", "The address does not belong to this account.");
        verify(response).sendRedirect("account");
    }

    @Test
    public void deleteThroughGetIsRejectedBeforeAnyChange() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("action")).thenReturn("delete");
        when(session.getAttribute("selectedId")).thenReturn(String.valueOf(OWNED_ADDRESS_ID));

        servlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        verifyNoInteractions(addressService);
    }

    @Test
    public void saveThroughGetIsRejectedBeforeAnyChange() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getParameter("action")).thenReturn("save");

        servlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        verifyNoInteractions(addressService);
    }

    @Test
    public void deletingOwnedAddressUsesTheSessionUserAndReportsSuccess() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("action")).thenReturn("delete");
        when(session.getAttribute("selectedId")).thenReturn(String.valueOf(OWNED_ADDRESS_ID));

        servlet.doPost(request, response);

        verify(addressService).deleteAddressById(OWNED_ADDRESS_ID, OWNER_ID);
        verify(session).setAttribute("msg", "Address deleted.");
        verify(addressesPage).forward(request, response);
    }

    @Test
    public void addressUsedInOrderRejectionIsShownAndListingIsNotRendered() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("action")).thenReturn("delete");
        when(session.getAttribute("selectedId")).thenReturn(String.valueOf(OWNED_ADDRESS_ID));
        doThrow(new StoreException("An address used for an order cannot be deleted."))
                .when(addressService).deleteAddressById(OWNED_ADDRESS_ID, OWNER_ID);

        servlet.doPost(request, response);

        verify(session).setAttribute("msg", "An address used for an order cannot be deleted.");
        verify(response).sendRedirect("account");
        verify(addressesPage, never()).forward(request, response);
    }

    @Test
    public void savingWithSelectedAddressUpdatesOwnedRecordWithSessionOwner() throws Exception {
        stubSaveParameters();
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("action")).thenReturn("save");
        when(session.getAttribute("selectedId")).thenReturn(String.valueOf(OWNED_ADDRESS_ID));

        servlet.doPost(request, response);

        ArgumentCaptor<AddressDTO> saved = ArgumentCaptor.forClass(AddressDTO.class);
        verify(addressService).updateAddressById(eq(OWNED_ADDRESS_ID), saved.capture());
        verify(addressService, never()).insertAddress(any());
        assertEquals(Integer.valueOf(OWNER_ID), saved.getValue().getUserId());
        assertEquals("Home", saved.getValue().getName());
        assertEquals(Integer.valueOf(20000000), saved.getValue().getPostalCode());
        verify(session).setAttribute("msg", "Address saved.");
    }

    @Test
    public void clientSuppliedUserIdIsIgnoredWhenInsertingNewAddress() throws Exception {
        stubSaveParameters();
        when(request.getParameter("userId")).thenReturn(String.valueOf(OTHER_USER_ID));
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("action")).thenReturn("save");
        when(session.getAttribute("selectedId")).thenReturn(null);

        servlet.doPost(request, response);

        ArgumentCaptor<AddressDTO> saved = ArgumentCaptor.forClass(AddressDTO.class);
        verify(addressService).insertAddress(saved.capture());
        verify(addressService, never()).updateAddressById(any(), any());
        assertEquals(Integer.valueOf(OWNER_ID), saved.getValue().getUserId());
    }

    @Test
    public void malformedSelectedIdDoesNotLookUpAnyAddress() throws Exception {
        when(request.getParameter("selectedId")).thenReturn("abc");

        servlet.doGet(request, response);

        verify(addressService, never()).findAddressById(any());
        verify(addressService, never()).addressBelongsToUser(any(), any());
        verify(editPage).forward(request, response);
    }

    @Test
    public void clearingSelectionForgetsSessionReferenceWithoutChangingRecords() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("action")).thenReturn("clear");

        servlet.doPost(request, response);

        verify(session).setAttribute("selectedId", null);
        verify(addressService, never()).insertAddress(any());
        verify(addressService, never()).updateAddressById(any(), any());
        verify(addressService, never()).deleteAddressById(any(), any());
    }

    private void stubSaveParameters() {
        when(request.getParameter("name")).thenReturn("Home");
        when(request.getParameter("zipcode")).thenReturn("20000000");
        when(request.getParameter("address")).thenReturn("Rua B, 10");
        when(request.getParameter("city")).thenReturn("Niteroi");
        when(request.getParameter("state")).thenReturn("RJ");
        when(request.getParameter("country")).thenReturn("Brasil");
    }
}
