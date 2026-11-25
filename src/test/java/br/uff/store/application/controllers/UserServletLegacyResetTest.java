package br.uff.store.application.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.uff.store.core.exceptions.PasswordResetRequiredException;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.services.IPasswordResetService;
import br.uff.store.core.interfaces.services.IRoleService;
import br.uff.store.core.interfaces.services.IUserService;
import br.uff.store.infrastructure.shared.Helper;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;

/** Decision 4A: signing in to an account with a legacy password hash sends a reset email instead. */
public class UserServletLegacyResetTest {
    private IUserService userService;
    private IPasswordResetService resetService;
    private UserServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    @Before
    public void setUp() throws Exception {
        userService = mock(IUserService.class);
        resetService = mock(IPasswordResetService.class);
        servlet = new UserServlet(userService, mock(IRoleService.class), new Helper(), resetService,
                "https://shop.example.test/store");
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestDispatcher("pages/login.jsp")).thenReturn(mock(RequestDispatcher.class));
        when(request.getParameter("action")).thenReturn("login");
        when(request.getParameter("email")).thenReturn("old@example.test");
        when(request.getParameter("password")).thenReturn("old-password");
        when(userService.login("old@example.test", "old-password"))
                .thenThrow(new PasswordResetRequiredException("old@example.test"));
    }

    @Test
    public void legacyAccountSignInSendsAResetLinkToTheConfiguredAddressAndSignsNobodyIn() throws Exception {
        servlet.doPost(request, response);

        verify(resetService).requestReset("old@example.test", "https://shop.example.test/store/password-reset");
        verify(session).setAttribute("msg", PasswordResetRequiredException.MESSAGE);
        verify(session, never()).setAttribute(eq("userId"), any());
        verify(request, never()).changeSessionId();
    }

    @Test
    public void legacyAccountSignInStillSignsNobodyInWhenTheEmailCannotBeSent() throws Exception {
        doThrow(new StoreException("Unable to send the reset email.")).when(resetService).requestReset(any(), any());

        servlet.doPost(request, response);

        verify(session).setAttribute("msg", "Unable to send the reset email.");
        verify(session, never()).setAttribute(eq("userId"), any());
    }
}
