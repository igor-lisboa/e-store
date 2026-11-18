package br.uff.store.core.services;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.Assert.fail;

import br.uff.store.core.dtos.AddressDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IAddressData;
import org.junit.Before;
import org.junit.Test;

public class AddressServiceTest {
    private IAddressData addressData;
    private AddressService service;

    @Before
    public void setUp() {
        addressData = mock(IAddressData.class);
        service = new AddressService(addressData);
    }

    @Test
    public void rejectsAddressUpdatesByAnotherAccount() throws Exception {
        when(addressData.addressBelongsToUser(7, 12)).thenReturn(false);

        try {
            service.updateAddressById(7, addressFor(12));
            fail("Expected an ownership error.");
        } catch (StoreException expected) {
            verify(addressData, never()).updateAddressById(eq(7), any(AddressDTO.class));
        }
    }

    @Test
    public void rejectsAddressDeletionByAnotherAccount() throws Exception {
        when(addressData.addressBelongsToUser(7, 12)).thenReturn(false);

        try {
            service.deleteAddressById(7, 12);
            fail("Expected an ownership error.");
        } catch (StoreException expected) {
            verify(addressData, never()).deleteAddressById(7);
        }
    }

    @Test
    public void doesNotDeleteAnAddressThatWasUsedForAPurchase() throws Exception {
        when(addressData.addressBelongsToUser(7, 12)).thenReturn(true);
        when(addressData.addressUsedInSale(7)).thenReturn(true);

        try {
            service.deleteAddressById(7, 12);
            fail("Expected a purchase history error.");
        } catch (StoreException expected) {
            verify(addressData, never()).deleteAddressById(7);
        }
    }

    private AddressDTO addressFor(Integer userId) {
        return new AddressDTO("Home", userId, 12345, "Main Street", "City", "State", "Country");
    }
}
