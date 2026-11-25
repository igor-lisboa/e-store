package br.uff.store.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.ISaleData;
import br.uff.store.core.interfaces.gateways.IDeliveryGateway;
import br.uff.store.core.interfaces.gateways.IPaymentGateway;
import br.uff.store.core.interfaces.services.ICartService;
import br.uff.store.core.interfaces.services.IAddressService;
import br.uff.store.core.dtos.AddressDTO;
import br.uff.store.core.dtos.CartItemDTO;
import br.uff.store.core.dtos.DeliveryResultDTO;
import br.uff.store.core.dtos.PaginationDTO;
import br.uff.store.core.dtos.PaymentResultDTO;
import br.uff.store.core.dtos.SaleDTO;
import br.uff.store.core.dtos.SaleLineDTO;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class SaleServiceTest {
    private final ISaleData sales = mock(ISaleData.class);
    private final ICartService carts = mock(ICartService.class);
    private final IAddressService addresses = mock(IAddressService.class);
    private final IPaymentGateway payments = mock(IPaymentGateway.class);
    private final IDeliveryGateway deliveries = mock(IDeliveryGateway.class);
    private final SaleService service = new SaleService(sales, carts, addresses, payments, deliveries);
    private final PaymentResultDTO payment = new PaymentResultDTO("test", "ref-1", PaymentResultDTO.AUTHORIZED, "raw-1");
    private final DeliveryResultDTO delivery = new DeliveryResultDTO("test", "STANDARD", "TRACK1", DeliveryResultDTO.PENDING, null);
    private final AddressDTO destination = new AddressDTO("Home", 2, 12345, "Main Street", "City", "State", "Country");

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
        readyToPurchase();

        service.recordSale(2, 7, 9);

        verify(sales).recordSale(7, 9, payment, delivery);
    }

    @Test
    public void paymentIsAuthorizedForTheSumOfTheCartLinesAndDeliveryGoesToTheChosenAddress() throws Exception {
        readyToPurchase();

        service.recordSale(2, 7, 9);

        verify(payments).authorize(2, new BigDecimal("30.50"));
        verify(deliveries).arrange(2, destination);
    }

    @Test
    public void declinedPaymentCreatesNoOrderAndNeverArrangesDelivery() throws Exception {
        readyToPurchase();
        when(payments.authorize(any(), any())).thenThrow(new StoreException("Payment declined."));

        assertDenied();

        verifyZeroInteractions(sales, deliveries);
    }

    @Test
    public void failedDeliveryReleasesThePaymentAndCreatesNoOrder() throws Exception {
        readyToPurchase();
        when(deliveries.arrange(any(), any())).thenThrow(new StoreException("No carrier available."));

        assertDenied();

        verify(payments).release(payment);
        verify(sales, never()).recordSale(anyInt(), anyInt(), any(), any());
    }

    @Test
    public void failedCheckoutReleasesThePaymentSoNoMoneyStaysReserved() throws Exception {
        readyToPurchase();
        doThrow(new StoreException("Requested quantity exceeds available stock.")).when(sales)
                .recordSale(eq(7), eq(9), any(), any());

        assertDenied();

        verify(payments).release(payment);
    }

    @Test
    public void failedReleaseIsKeptAsSuppressedAndTheOriginalErrorSurvives() throws Exception {
        readyToPurchase();
        StoreException original = new StoreException("Requested quantity exceeds available stock.");
        doThrow(original).when(sales).recordSale(eq(7), eq(9), any(), any());
        StoreException releaseFailure = new StoreException("Release unavailable.");
        doThrow(releaseFailure).when(payments).release(payment);

        try {
            service.recordSale(2, 7, 9);
            fail("The checkout failure must reach the caller.");
        } catch (StoreException thrown) {
            assertSame(original, thrown);
            assertTrue(Arrays.asList(thrown.getSuppressed()).contains(releaseFailure));
        }
    }

    private void readyToPurchase() throws Exception {
        when(carts.isValidActiveCart(7, 2)).thenReturn(true);
        when(addresses.addressBelongsToUser(9, 2)).thenReturn(true);
        when(addresses.findAddressById(9)).thenReturn(destination);
        CartItemDTO first = new CartItemDTO();
        first.setTotalPrice(new BigDecimal("20.00"));
        CartItemDTO second = new CartItemDTO();
        second.setTotalPrice(new BigDecimal("10.50"));
        when(carts.listCartProducts(7)).thenReturn(Arrays.asList(first, second));
        when(payments.authorize(2, new BigDecimal("30.50"))).thenReturn(payment);
        when(deliveries.arrange(2, destination)).thenReturn(delivery);
    }

    private void assertDenied() throws Exception {
        try {
            service.recordSale(2, 7, 9);
            fail("Unverified ownership must not permit checkout.");
        } catch (StoreException expected) {
            // Persistence is separately verified untouched by the caller.
        }
    }

    @Test
    public void cancellationIsDelegatedWithTheCallersIdentity() throws Exception {
        service.cancelSale(2, 11);
        verify(sales).cancelSale(11, 2);
    }

    @Test
    public void cancellationRejectsInvalidIdentifiersBeforePersistence() throws Exception {
        Integer[][] invalid = {{null, 11}, {2, null}, {0, 11}, {2, -1}};
        for (Integer[] ids : invalid) {
            try {
                service.cancelSale(ids[0], ids[1]);
                fail("Invalid identifiers must be rejected.");
            } catch (StoreException expected) {
                // Expected: nothing reaches persistence.
            }
        }
        verify(sales, never()).cancelSale(anyInt(), anyInt());
    }

    @Test
    public void orderHistoryCarriesTheSnapshotLinesOfEachSale() throws Exception {
        SaleDTO sale = new SaleDTO();
        sale.setId(4);
        List<SaleLineDTO> lines = Collections.singletonList(new SaleLineDTO());
        when(sales.listUserSales(2, 5, 1)).thenReturn(new PaginationDTO<>(1, Collections.singletonList(sale), 1));
        when(sales.listSaleLines(4)).thenReturn(lines);

        List<SaleDTO> result = service.listUserSales(2, 5, 1).getItems();

        assertEquals(1, result.size());
        assertSame(lines, result.get(0).getLines());
    }
}
