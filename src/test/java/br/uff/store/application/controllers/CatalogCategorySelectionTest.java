package br.uff.store.application.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.junit.Test;

public class CatalogCategorySelectionTest {
    @Test
    public void categoryLinksSelectTheMatchingPlatformAndProductType() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getParameter("categoryId")).thenReturn("9");
        List<String> platforms = new ArrayList<>();
        List<String> types = new ArrayList<>();

        CatalogCategorySelection.apply(request, session, platforms, types);

        assertEquals(Arrays.asList("xbox"), platforms);
        assertEquals(Arrays.asList("games"), types);
        verify(session).setAttribute("games", "checked");
    }

    @Test
    public void invalidCategoryLinksDoNotAddFilters() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("categoryId")).thenReturn("999");
        List<String> platforms = new ArrayList<>();
        List<String> types = new ArrayList<>();

        CatalogCategorySelection.apply(request, mock(HttpSession.class), platforms, types);

        assertTrue(platforms.isEmpty());
        assertTrue(types.isEmpty());
    }
}
