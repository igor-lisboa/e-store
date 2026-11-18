package br.uff.store.application.controllers;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ProductsServletTest {
    @Test
    public void missingOrUnparseablePageSizeUsesTheDefaultOfTwenty() {
        assertEquals(20, ProductsServlet.resolvePageSize(null));
        assertEquals(20, ProductsServlet.resolvePageSize(""));
        assertEquals(20, ProductsServlet.resolvePageSize("twenty"));
        assertEquals(20, ProductsServlet.resolvePageSize("2.5"));
    }

    @Test
    public void pageSizeBoundariesAreAcceptedInclusively() {
        assertEquals(1, ProductsServlet.resolvePageSize("1"));
        assertEquals(12, ProductsServlet.resolvePageSize("12"));
        assertEquals(100, ProductsServlet.resolvePageSize("100"));
    }

    @Test
    public void pageSizesOutsideOneToOneHundredFallBackToTheDefault() {
        assertEquals(20, ProductsServlet.resolvePageSize("0"));
        assertEquals(20, ProductsServlet.resolvePageSize("-5"));
        assertEquals(20, ProductsServlet.resolvePageSize("101"));
        assertEquals(20, ProductsServlet.resolvePageSize("2147483648"));
    }
}
