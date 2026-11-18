package br.uff.store.application.security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class AccountRedirects {
    private static final Set<String> DESTINATIONS = new HashSet<>(Arrays.asList(
            "account", "addresses", "cart", "checkout", "product", "products", "reviews", "product-admin"));

    private AccountRedirects() { }

    public static String allowed(String destination) {
        return DESTINATIONS.contains(destination) ? destination : null;
    }
}
