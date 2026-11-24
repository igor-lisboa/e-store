package br.uff.store.core.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.uff.store.core.dtos.CartDTO;
import br.uff.store.core.interfaces.data.ICartData;
import br.uff.store.core.interfaces.data.IProductData;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;

/** Covers anonymous cart expiry and sign-in handoff, including ownership and stale-cart denial. */
public class CartActivityServiceTest {
    private static final String NOW = "2026-03-01 10:00:00";
    private static final String EXPIRY_CUTOFF = "2026-01-30 10:00:00";
    private static final String IP = "203.0.113.7";
    private static final int CUSTOMER_ID = 2;

    private ICartData cartData;
    private CartService service;

    @Before
    public void setUp() {
        cartData = mock(ICartData.class);
        IProductData productData = mock(IProductData.class);
        Clock clock = Clock.fixed(Instant.parse("2026-03-01T10:00:00Z"), ZoneOffset.UTC);
        service = new CartService(cartData, productData, clock);
    }

    @Test
    public void staleAnonymousSessionCartIsNotReusedAndANewCartIsCreated() throws Exception {
        givenLiveUnsoldSessionCart(5);
        when(cartData.isCartUnowned(5)).thenReturn(true);
        when(cartData.isCartActiveSince(5, EXPIRY_CUTOFF)).thenReturn(false);
        givenNewCart(9, null);

        CartDTO cart = service.getActiveCart(5, null, IP);

        assertEquals(Integer.valueOf(9), cart.getId());
        verify(cartData).createCart(IP, NOW, null);
        verify(cartData, never()).touchCart(eq(5), anyString());
    }

    @Test
    public void recentAnonymousSessionCartIsReusedAndItsActivityIsRecorded() throws Exception {
        givenLiveUnsoldSessionCart(5);
        when(cartData.isCartUnowned(5)).thenReturn(true);
        when(cartData.isCartActiveSince(5, EXPIRY_CUTOFF)).thenReturn(true);
        when(cartData.findCart(5)).thenReturn(storedCart(5, null));

        CartDTO cart = service.getActiveCart(5, null, IP);

        assertEquals(Integer.valueOf(5), cart.getId());
        verify(cartData, never()).createCart(anyString(), anyString(), any());
        verify(cartData).touchCart(5, NOW);
    }

    @Test
    public void anonymousVisitorCannotReuseAnotherAccountsCart() throws Exception {
        when(cartData.cartExists(5)).thenReturn(true);
        when(cartData.isCartSold(5)).thenReturn(false);
        when(cartData.isCartUnowned(5)).thenReturn(false);
        givenNewCart(9, null);

        CartDTO cart = service.getActiveCart(5, null, IP);

        assertEquals(Integer.valueOf(9), cart.getId());
        verify(cartData).createCart(IP, NOW, null);
        verify(cartData, never()).touchCart(eq(5), anyString());
    }

    @Test
    public void soldSessionCartIsNeverReused() throws Exception {
        when(cartData.cartExists(5)).thenReturn(true);
        when(cartData.isCartSold(5)).thenReturn(true);
        givenNewCart(9, CUSTOMER_ID);

        CartDTO cart = service.getActiveCart(5, CUSTOMER_ID, IP);

        assertEquals(Integer.valueOf(9), cart.getId());
        verify(cartData, never()).mergeCart(anyInt(), anyInt());
        verify(cartData, never()).touchCart(eq(5), anyString());
    }

    @Test
    public void signInMergesAnonymousCartIntoTheAccountCart() throws Exception {
        givenLiveUnsoldSessionCart(5);
        when(cartData.isCartUnowned(5)).thenReturn(true);
        when(cartData.isCartActiveSince(5, EXPIRY_CUTOFF)).thenReturn(true);
        when(cartData.findCart(5)).thenReturn(storedCart(5, null));
        when(cartData.listUnsoldUserCarts(CUSTOMER_ID))
                .thenReturn(Collections.singletonList(storedCart(7, CUSTOMER_ID)));

        CartDTO cart = service.getActiveCart(5, CUSTOMER_ID, IP);

        assertEquals(Integer.valueOf(7), cart.getId());
        verify(cartData).mergeCart(5, 7);
        verify(cartData, never()).assignUnownedCart(anyInt(), anyInt());
        verify(cartData).touchCart(7, NOW);
    }

    @Test
    public void signInClaimsAnonymousCartWhenTheAccountHasNoCart() throws Exception {
        givenLiveUnsoldSessionCart(5);
        when(cartData.isCartUnowned(5)).thenReturn(true);
        when(cartData.isCartActiveSince(5, EXPIRY_CUTOFF)).thenReturn(true);
        when(cartData.findCart(5))
                .thenReturn(storedCart(5, null))
                .thenReturn(storedCart(5, CUSTOMER_ID));

        CartDTO cart = service.getActiveCart(5, CUSTOMER_ID, IP);

        assertEquals(Integer.valueOf(5), cart.getId());
        verify(cartData).assignUnownedCart(5, CUSTOMER_ID);
        verify(cartData, never()).mergeCart(anyInt(), anyInt());
    }

    @Test
    public void expiredAnonymousCartIsNotMergedOnSignIn() throws Exception {
        givenLiveUnsoldSessionCart(5);
        when(cartData.isCartUnowned(5)).thenReturn(true);
        when(cartData.isCartActiveSince(5, EXPIRY_CUTOFF)).thenReturn(false);
        when(cartData.listUnsoldUserCarts(CUSTOMER_ID))
                .thenReturn(Collections.singletonList(storedCart(7, CUSTOMER_ID)));

        CartDTO cart = service.getActiveCart(5, CUSTOMER_ID, IP);

        assertEquals(Integer.valueOf(7), cart.getId());
        verify(cartData, never()).mergeCart(anyInt(), anyInt());
        verify(cartData, never()).assignUnownedCart(anyInt(), anyInt());
    }

    private void givenLiveUnsoldSessionCart(Integer cartId) throws Exception {
        when(cartData.cartExists(cartId)).thenReturn(true);
        when(cartData.isCartSold(cartId)).thenReturn(false);
    }

    private void givenNewCart(Integer newCartId, Integer userId) throws Exception {
        when(cartData.findCart(eq(IP), eq(NOW), eq(userId))).thenReturn(storedCart(newCartId, userId));
    }

    private CartDTO storedCart(Integer id, Integer userId) {
        CartDTO cart = new CartDTO();
        cart.setId(id);
        cart.setUserId(userId);
        cart.setIp(IP);
        return cart;
    }
}
