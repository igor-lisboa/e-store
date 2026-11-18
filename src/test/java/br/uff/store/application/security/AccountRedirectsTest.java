package br.uff.store.application.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class AccountRedirectsTest {
    @Test
    public void onlyKnownLocalRoutesCanBeUsedAfterAuthentication() {
        assertEquals("cart", AccountRedirects.allowed("cart"));
        assertEquals("reviews", AccountRedirects.allowed("reviews"));
        for (String destination : new String[] {null, "https://example.test", "//example.test",
                "javascript:alert(1)", "../admin", "cart?redirect=https://example.test", "<script>"}) {
            assertNull(AccountRedirects.allowed(destination));
        }
    }
}
