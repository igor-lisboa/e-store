package br.uff.store.application.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.services.IPasswordResetService;
import java.sql.SQLException;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;

public class PasswordResetServletTest {
    private IPasswordResetService service;
    private PasswordResetServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    @Before
    public void setUp() {
        service = mock(IPasswordResetService.class);
        servlet = new PasswordResetServlet(service, "https://shop.example.test");
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);
        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher("pages/password-reset.jsp")).thenReturn(dispatcher);
    }

    private void parameter(String name, String value) {
        when(request.getParameter(name)).thenReturn(value);
    }

    @Test
    public void theRequestPageShowsTheEmailFormWhenThereIsNoToken() throws Exception {
        servlet.doGet(request, response);

        verify(request).setAttribute("resetToken", "");
        verify(dispatcher).forward(request, response);
    }

    @Test
    public void theLinkPageCarriesTheTokenToTheForm() throws Exception {
        parameter("token", " abc ");

        servlet.doGet(request, response);

        verify(request).setAttribute("resetToken", "abc");
    }

    @Test
    public void requestingALinkAnswersTheSameWhetherOrNotTheAccountExists() throws Exception {
        parameter("action", "request");
        parameter("email", "ana@example.test");

        servlet.doPost(request, response);

        verify(service).requestReset("ana@example.test", "https://shop.example.test/password-reset");
        verify(session).setAttribute("msg", "If the account exists, a reset link was sent to that email address.");
        verify(response).sendRedirect("account");
    }

    @Test
    public void completingSetsThePasswordAndSendsTheVisitorToSignIn() throws Exception {
        parameter("action", "complete");
        parameter("token", "abc");
        parameter("password", "brand-new-password");

        servlet.doPost(request, response);

        verify(service).resetPassword("abc", "brand-new-password");
        verify(session).setAttribute("msg", "Password updated. Sign in with your new password.");
        verify(response).sendRedirect("account");
    }

    @Test
    public void aRejectedTokenShowsASafeMessageAndKeepsTheTokenInTheReturnAddress() throws Exception {
        parameter("action", "complete");
        parameter("token", "a b&c");
        parameter("password", "brand-new-password");
        doThrow(new StoreException("The reset link is invalid or has expired.")).when(service).resetPassword(any(), any());

        servlet.doPost(request, response);

        verify(session).setAttribute("msg", "The reset link is invalid or has expired.");
        verify(response).sendRedirect("password-reset?token=a+b%26c");
    }

    @Test
    public void unexpectedFailuresNeverShowInternalDetails() throws Exception {
        parameter("action", "request");
        parameter("email", "ana@example.test");
        doThrow(new IllegalStateException("jdbc:mysql://db:3306 refused", new SQLException("x"))).when(service).requestReset(any(), any());

        servlet.doPost(request, response);

        verify(session).setAttribute("msg", "The request could not be completed.");
        verify(response).sendRedirect("password-reset");
    }

    @Test
    public void unknownActionsAreRejectedWithoutTouchingAnyAccount() throws Exception {
        parameter("action", "delete");

        servlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST);
        verify(service, never()).requestReset(any(), any());
        verify(service, never()).resetPassword(any(), any());
    }

    @Test
    public void withoutAConfiguredBaseTheLinkUsesTheRequestsOwnAddress() {
        when(request.getScheme()).thenReturn("http");
        when(request.getServerName()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(8181);
        when(request.getContextPath()).thenReturn("/store");

        assertEquals("http://localhost:8181/store/password-reset", br.uff.store.application.ResetLinks.pageUrl(request, null));
        assertEquals("https://shop.example.test/password-reset", br.uff.store.application.ResetLinks.pageUrl(request, " https://shop.example.test// "));
    }
}
