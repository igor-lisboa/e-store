package br.uff.store.core.services;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.ISaleData;
import br.uff.store.core.interfaces.services.ICartService;
import br.uff.store.core.interfaces.services.IAddressService;
import org.junit.Test;

public class SaleServiceTest {
    private final ISaleData sales = mock(ISaleData.class);
    private final ICartService carts = mock(ICartService.class);
    private final IAddressService addresses = mock(IAddressService.class);
    private final SaleService service = new SaleService(sales, carts, addresses);

    @Test
    public void invalidOrUnknownCartOwnershipCannotCreateASale() throws Exception {
        for (Boolean allowed : new Boolean[] {false, null}) {
            when(carts.isValidActiveCart(7, 2)).thenReturn(allowed);
            assertDenied();
            verifyZeroInteractions(addresses, sales);
        }
    }

    @Test
    public void anotherAccountsAddressCannotCreateASale() throws Exception {
        when(carts.isValidActiveCart(7, 2)).thenReturn(true);
        when(addresses.addressBelongsToUser(9, 2)).thenReturn(false);
        assertDenied();
        verifyZeroInteractions(sales);
    }

    @Test
    public void ownedActiveCartAndAddressReachTheAtomicCheckoutBoundary() throws Exception {
        when(carts.isValidActiveCart(7, 2)).thenReturn(true);
        when(addresses.addressBelongsToUser(9, 2)).thenReturn(true);
        service.recordSale(2, 7, 9);
        verify(sales).recordSale(7, 9);
    }

    private void assertDenied() throws Exception {
        try {
            service.recordSale(2, 7, 9);
            fail("Unverified ownership must not permit checkout.");
        } catch (StoreException expected) {
            // Persistence is separately verified untouched by the caller.
        }
    }
}
