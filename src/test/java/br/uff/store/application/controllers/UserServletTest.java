package br.uff.store.application.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.uff.store.core.dtos.UserDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.services.IRoleService;
import br.uff.store.core.interfaces.services.IUserService;
import br.uff.store.infrastructure.shared.Helper;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class UserServletTest {
    private static final int ADMIN_ROLE = 1;
    private static final int CUSTOMER_ROLE = 2;

    private IUserService userService;
    private IRoleService roleService;
    private UserServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    @Before
    public void setUp() throws Exception {
        userService = mock(IUserService.class);
        roleService = mock(IRoleService.class);
        servlet = new UserServlet(userService, roleService, new Helper());
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(request.getContextPath()).thenReturn("");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestDispatcher("pages/login.jsp")).thenReturn(mock(RequestDispatcher.class));
    }

    private void signedInAs(int userId, int roleId) {
        when(session.getAttribute("userId")).thenReturn(userId);
        when(session.getAttribute("userRole")).thenReturn(roleId);
    }

    private void parameter(String name, String value) {
        when(request.getParameter(name)).thenReturn(value);
    }

    @Test
    public void accountChangesByGetAreRejectedBecauseTheyAlterData() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        for (String action : new String[] {"save", "login", "register", "logout"}) {
            parameter("action", action);
            servlet.doGet(request, response);
        }

        verify(response, org.mockito.Mockito.times(4)).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        verify(userService, never()).saveUser(any());
        verify(userService, never()).login(any(), any());
    }

    @Test
    public void customerCannotEditAnotherCustomersAccount() throws Exception {
        signedInAs(22, CUSTOMER_ROLE);
        parameter("action", "save");
        parameter("selectedId", "33");
        when(session.getAttribute("selUser")).thenReturn("33");

        servlet.doPost(request, response);

        verify(userService, never()).saveUser(any());
        verify(session).setAttribute("msg", "You are not allowed to edit this account.");
    }

    @Test
    public void customerCannotPromoteThemselvesBecauseTheStoredRoleIsKept() throws Exception {
        signedInAs(22, CUSTOMER_ROLE);
        parameter("action", "save");
        parameter("name", "Me");
        parameter("email", "me@example.test");
        parameter("roleId", String.valueOf(ADMIN_ROLE));
        when(session.getAttribute("selUser")).thenReturn("22");
        when(userService.findUserById(22)).thenReturn(new UserDTO(22, "Me", "me@example.test", null, CUSTOMER_ROLE));

        servlet.doPost(request, response);

        ArgumentCaptor<UserDTO> saved = ArgumentCaptor.forClass(UserDTO.class);
        verify(userService).saveUser(saved.capture());
        assertEquals(Integer.valueOf(CUSTOMER_ROLE), saved.getValue().getRoleId());
        assertEquals(Integer.valueOf(22), saved.getValue().getId());
    }

    @Test
    public void administratorMayChangeAnotherAccountsRole() throws Exception {
        signedInAs(1, ADMIN_ROLE);
        parameter("action", "save");
        parameter("name", "Staff");
        parameter("email", "staff@example.test");
        parameter("roleId", String.valueOf(ADMIN_ROLE));
        when(session.getAttribute("selUser")).thenReturn("33");

        servlet.doPost(request, response);

        ArgumentCaptor<UserDTO> saved = ArgumentCaptor.forClass(UserDTO.class);
        verify(userService).saveUser(saved.capture());
        assertEquals(Integer.valueOf(33), saved.getValue().getId());
        assertEquals(Integer.valueOf(ADMIN_ROLE), saved.getValue().getRoleId());
    }

    @Test
    public void registrationAlwaysCreatesACustomerEvenIfTheRequestAsksForAdministrator() throws Exception {
        parameter("action", "register");
        parameter("name", "New");
        parameter("email", "new@example.test");
        parameter("password", "long-enough-password");
        parameter("roleId", String.valueOf(ADMIN_ROLE));
        when(userService.saveUser(any())).thenReturn(new UserDTO(50, "New", "new@example.test", null, CUSTOMER_ROLE));

        servlet.doPost(request, response);

        ArgumentCaptor<UserDTO> saved = ArgumentCaptor.forClass(UserDTO.class);
        verify(userService).saveUser(saved.capture());
        assertEquals(Integer.valueOf(CUSTOMER_ROLE), saved.getValue().getRoleId());
        verify(request).changeSessionId();
        verify(session).setAttribute("userId", 50);
    }

    @Test
    public void successfulLoginRotatesTheSessionAndRemembersOnlyTheEmail() throws Exception {
        parameter("action", "login");
        parameter("email", "customer@example.test");
        parameter("password", "development-password");
        parameter("remember", "on");
        when(userService.login("customer@example.test", "development-password"))
                .thenReturn(new UserDTO(2, "Customer", "customer@example.test", null, CUSTOMER_ROLE));

        servlet.doPost(request, response);

        verify(request).changeSessionId();
        verify(session).setAttribute("userId", 2);
        verify(session).setAttribute("userRole", CUSTOMER_ROLE);
        ArgumentCaptor<Cookie> cookies = ArgumentCaptor.forClass(Cookie.class);
        verify(response, atLeastOnce()).addCookie(cookies.capture());
        boolean emailRemembered = false;
        for (Cookie cookie : cookies.getAllValues()) {
            assertFalse("a password must never be stored in a cookie", cookie.getValue().contains("development-password"));
            if ("loginEmail".equals(cookie.getName())) {
                emailRemembered = true;
                assertEquals("customer@example.test", cookie.getValue());
            }
            if ("loginPassword".equals(cookie.getName())) {
                assertEquals("legacy password cookies are expired", 0, cookie.getMaxAge());
            }
        }
        assertTrue(emailRemembered);
    }

    @Test
    public void failedLoginSignsNobodyInAndKeepsTheSession() throws Exception {
        parameter("action", "login");
        parameter("email", "customer@example.test");
        parameter("password", "wrong");
        when(userService.login("customer@example.test", "wrong")).thenThrow(new StoreException("Invalid email or password."));

        servlet.doPost(request, response);

        verify(request, never()).changeSessionId();
        verify(session, never()).setAttribute(org.mockito.ArgumentMatchers.eq("userId"), any());
        verify(session).setAttribute("msg", "Invalid email or password.");
    }

    @Test
    public void failedLoginFeedbackIsShownInTheVisitorsChosenLanguage() throws Exception {
        when(session.getAttribute("appLocale")).thenReturn(new java.util.Locale("pt", "BR"));
        parameter("action", "login");
        parameter("email", "customer@example.test");
        parameter("password", "wrong");
        when(userService.login("customer@example.test", "wrong")).thenThrow(new StoreException("Invalid email or password."));

        servlet.doPost(request, response);

        verify(session).setAttribute("msg", "E-mail ou senha inválidos.");
    }

    @Test
    public void successfulLoginGreetsInTheVisitorsChosenLanguage() throws Exception {
        when(session.getAttribute("appLocale")).thenReturn(new java.util.Locale("es"));
        parameter("action", "login");
        parameter("email", "customer@example.test");
        parameter("password", "development-password");
        when(userService.login("customer@example.test", "development-password"))
                .thenReturn(new UserDTO(2, "Ana", "customer@example.test", null, CUSTOMER_ROLE));

        servlet.doPost(request, response);

        verify(session).setAttribute("msg", "¡Bienvenido, Ana!");
    }

    @Test
    public void unexpectedLoginFailureIsNotShownToTheVisitor() throws Exception {
        parameter("action", "login");
        parameter("email", "customer@example.test");
        parameter("password", "x");
        when(userService.login(any(), any())).thenThrow(new IllegalStateException("jdbc:mysql://db/internal secret"));

        servlet.doPost(request, response);

        verify(session).setAttribute("msg", "The request could not be completed.");
    }

    @Test
    public void logoutEndsTheSession() throws Exception {
        signedInAs(2, CUSTOMER_ROLE);
        parameter("action", "logout");

        servlet.doPost(request, response);

        verify(session).invalidate();
        verify(response).sendRedirect("");
    }

    @Test
    public void customerCannotOpenAnotherAccountPage() throws Exception {
        signedInAs(22, CUSTOMER_ROLE);
        when(session.getAttribute("selUser")).thenReturn("33");

        servlet.doGet(request, response);

        verify(session).setAttribute("selUser", null);
        verify(session).setAttribute("msg", "Access is not permitted.");
        verify(userService, never()).findUserById(33);
    }

    @Test
    public void customerListingContainsOnlyTheirOwnAccount() throws Exception {
        signedInAs(22, CUSTOMER_ROLE);
        UserDTO own = new UserDTO(22, "Me", "me@example.test", null, CUSTOMER_ROLE);
        when(userService.findUserById(22)).thenReturn(own);
        RequestDispatcher page = mock(RequestDispatcher.class);
        when(request.getRequestDispatcher("pages/accounts.jsp")).thenReturn(page);

        servlet.doGet(request, response);

        ArgumentCaptor<Object> users = ArgumentCaptor.forClass(Object.class);
        verify(request).setAttribute(org.mockito.ArgumentMatchers.eq("users"), users.capture());
        assertEquals(java.util.Collections.singletonList(own), users.getValue());
        verify(userService, never()).listUsers();
        verify(page).forward(request, response);
    }

    @Test
    public void administratorListingContainsEveryAccount() throws Exception {
        signedInAs(1, ADMIN_ROLE);
        List<UserDTO> everyone = java.util.Arrays.asList(new UserDTO(1, "A", "a@x", null, ADMIN_ROLE),
                new UserDTO(2, "B", "b@x", null, CUSTOMER_ROLE));
        when(userService.listUsers()).thenReturn(everyone);
        when(request.getRequestDispatcher("pages/accounts.jsp")).thenReturn(mock(RequestDispatcher.class));

        servlet.doGet(request, response);

        verify(request).setAttribute("users", everyone);
    }

    @Test
    public void signInRedirectOnlyFollowsKnownLocalDestinations() throws Exception {
        signedInAs(2, CUSTOMER_ROLE);
        parameter("redirect", "https://evil.example/phish");
        when(userService.findUserById(2)).thenReturn(new UserDTO(2, "C", "c@x", null, CUSTOMER_ROLE));
        when(request.getRequestDispatcher("pages/accounts.jsp")).thenReturn(mock(RequestDispatcher.class));

        servlet.doGet(request, response);

        verify(response, never()).sendRedirect("https://evil.example/phish");
    }

    @Test
    public void signedInVisitorFollowsAnAllowedRedirect() throws Exception {
        signedInAs(2, CUSTOMER_ROLE);
        parameter("redirect", "cart");

        servlet.doGet(request, response);

        verify(response).sendRedirect("cart");
    }
}
