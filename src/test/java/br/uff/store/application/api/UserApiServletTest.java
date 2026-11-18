package br.uff.store.application.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import br.uff.store.core.dtos.UserDTO;
import br.uff.store.core.exceptions.ResourceNotFoundException;
import br.uff.store.core.interfaces.services.IUserService;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;

public class UserApiServletTest {
    private IUserService userService;
    private UserApiServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter responseBody;

    @Before
    public void setUp() throws Exception {
        userService = mock(IUserService.class);
        servlet = new UserApiServlet(userService);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        responseBody = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
    }

    @Test
    public void rejectsRequestsWithoutAnAuthenticatedSession() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertEquals("{\"error\":\"Authentication is required.\"}", responseBody.toString());
        verifyZeroInteractions(userService);
    }

    @Test
    public void preventsCustomersFromReadingAnotherAccount() throws Exception {
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(12);
        when(request.getPathInfo()).thenReturn("/13");
        when(userService.isAdmin(12)).thenReturn(false);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(userService).isAdmin(12);
        assertFalse(responseBody.toString().contains("password"));
    }

    @Test
    public void malformedUserIdsReturnAClientError() throws Exception {
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(12);
        when(request.getPathInfo()).thenReturn("/not-an-id");
        when(userService.isAdmin(12)).thenReturn(false);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertEquals("{\"error\":\"A valid user ID is required.\"}", responseBody.toString());
    }

    @Test
    public void customersCanReadTheirAccountWithoutExposingPasswordData() throws Exception {
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(12);
        when(request.getPathInfo()).thenReturn("/12");
        when(userService.isAdmin(12)).thenReturn(false);
        when(userService.findUserById(12)).thenReturn(
                new UserDTO(12, "Customer", "customer@example.test", "stored-hash", 2));

        servlet.doGet(request, response);

        assertEquals("{\"id\":12,\"name\":\"Customer\",\"email\":\"customer@example.test\",\"roleId\":2}",
                responseBody.toString());
        assertFalse(responseBody.toString().contains("stored-hash"));
    }

    @Test
    public void anAdministratorGetsNotFoundForAnAbsentAccount() throws Exception {
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(12);
        when(request.getPathInfo()).thenReturn("/99");
        when(userService.isAdmin(12)).thenReturn(true);
        when(userService.findUserById(99)).thenThrow(new ResourceNotFoundException("User"));
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        assertEquals("{\"error\":\"User was not found.\"}", responseBody.toString());
    }
}
