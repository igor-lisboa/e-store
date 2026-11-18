package br.uff.store.application.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import javax.servlet.http.HttpSession;
import org.junit.Test;

public class CsrfTokensTest {
    @Test
    public void newTokensHaveSufficientEntropyAndAreSafeForHtmlAttributes() {
        HttpSession firstSession = mock(HttpSession.class);
        String first = CsrfTokens.getOrCreate(firstSession);
        String second = CsrfTokens.getOrCreate(mock(HttpSession.class));

        assertEquals(43, first.length());
        assertTrue(first.matches("[A-Za-z0-9_-]+"));
        assertNotEquals(first, second);
        verify(firstSession).setAttribute(CsrfTokens.SESSION_ATTRIBUTE, first);
    }

    @Test
    public void existingSessionTokensRemainValidAcrossMultipleForms() {
        HttpSession session = mock(HttpSession.class);
        String token = CsrfTokens.getOrCreate(session);
        when(session.getAttribute(CsrfTokens.SESSION_ATTRIBUTE)).thenReturn(token);

        assertEquals(token, CsrfTokens.getOrCreate(session));
        assertTrue(CsrfTokens.matches(session, token));
        assertFalse(CsrfTokens.matches(session, "incorrect"));
    }

    @Test
    public void missingTokensAndSessionsAreRejected() {
        assertFalse(CsrfTokens.matches(null, "token"));
        assertFalse(CsrfTokens.matches(mock(HttpSession.class), null));
        assertFalse(CsrfTokens.matches(mock(HttpSession.class), "token"));
    }
}
