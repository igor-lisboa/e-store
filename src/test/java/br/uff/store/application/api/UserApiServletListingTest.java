package br.uff.store.application.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.uff.store.core.dtos.UserDTO;
import br.uff.store.core.interfaces.services.IUserService;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;

/** Listing, path handling, damaged sessions, and failures for the account API. */
public class UserApiServletListingTest {
    private IUserService userService;
    private UserApiServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private StringWriter body;

    @Before
    public void setUp() throws Exception {
        userService = mock(IUserService.class);
        servlet = new UserApiServlet(userService);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(12);
    }

    @Test
    public void anAdministratorListsEveryAccountWithoutPasswordData() throws Exception {
        when(userService.isAdmin(12)).thenReturn(true);
        when(userService.listUsers()).thenReturn(Arrays.asList(
                new UserDTO(1, "Admin", "admin@example.test", "hash-1", 1),
                new UserDTO(2, "Customer", "customer@example.test", "hash-2", 2)));
        when(request.getPathInfo()).thenReturn("/");

        servlet.doGet(request, response);

        assertEquals("[{\"id\":1,\"name\":\"Admin\",\"email\":\"admin@example.test\",\"roleId\":1},"
                + "{\"id\":2,\"name\":\"Customer\",\"email\":\"customer@example.test\",\"roleId\":2}]", body.toString());
        assertFalse(body.toString().contains("hash"));
    }

    @Test
    public void aCustomerCannotListAccountsWhetherThePathIsMissingOrEmpty() throws Exception {
        when(userService.isAdmin(12)).thenReturn(false);
        for (String path : new String[] {null, "", "/"}) {
            when(request.getPathInfo()).thenReturn(path);
            servlet.doGet(request, response);
        }

        verify(response, org.mockito.Mockito.times(3)).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(userService, never()).listUsers();
    }

    @Test
    public void idsThatAreNotASinglePositiveNumberAreClientErrors() throws Exception {
        when(userService.isAdmin(12)).thenReturn(true);
        for (String path : new String[] {"/0", "/-4", "/12/extra", "/12.5", "/99999999999"}) {
            when(request.getPathInfo()).thenReturn(path);
            servlet.doGet(request, response);
        }

        verify(response, org.mockito.Mockito.times(5)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(userService, never()).findUserById(anyInt());
    }

    @Test
    public void aDamagedSessionIdentityIsTreatedAsNotSignedIn() throws Exception {
        when(session.getAttribute("userId")).thenReturn("twelve");
        when(request.getPathInfo()).thenReturn("/12");

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(userService, never()).findUserById(anyInt());
    }

    @Test
    public void anUnexpectedFailureReturnsAGenericServerError() throws Exception {
        when(userService.isAdmin(12)).thenThrow(new IllegalStateException("jdbc:mysql://db refused", new SQLException()));
        when(request.getPathInfo()).thenReturn("/12");

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        assertEquals("{\"error\":\"The request could not be completed.\"}", body.toString());
    }
}
