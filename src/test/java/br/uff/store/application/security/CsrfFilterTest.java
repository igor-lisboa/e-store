package br.uff.store.application.security;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;

public class CsrfFilterTest {
    private final CsrfFilter filter = new CsrfFilter();
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;
    private HttpSession session;

    @Before
    public void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
        session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession(true)).thenReturn(session);
    }

    @Test
    public void getRendersFormsWithASessionToken() throws Exception {
        when(request.getMethod()).thenReturn("GET");

        filter.doFilter(request, response, chain);

        verify(session).setAttribute(eq(CsrfTokens.SESSION_ATTRIBUTE), anyString());
        verify(response).setHeader("Cache-Control", "no-store");
        verify(chain).doFilter(request, response);
    }

    @Test
    public void postWithoutTokenNeverReachesTheController() throws Exception {
        when(request.getMethod()).thenReturn("POST");

        filter.doFilter(request, response, chain);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verifyZeroInteractions(chain);
        verify(request, never()).getSession(true);
    }

    @Test
    public void tokenFromAnotherSessionIsRejected() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("_csrf")).thenReturn("other-session-token");
        when(session.getAttribute(CsrfTokens.SESSION_ATTRIBUTE)).thenReturn("expected-token");

        filter.doFilter(request, response, chain);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verifyZeroInteractions(chain);
    }

    @Test
    public void validPostReachesTheController() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getParameter("_csrf")).thenReturn("expected-token");
        when(session.getAttribute(CsrfTokens.SESSION_ATTRIBUTE)).thenReturn("expected-token");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    public void expiredSessionCannotBeRecreatedByAnUnverifiedPost() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getSession(false)).thenReturn(null);
        when(request.getParameter("_csrf")).thenReturn("expired-token");

        filter.doFilter(request, response, chain);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verifyZeroInteractions(chain);
        verify(request, never()).getSession(true);
    }
}
