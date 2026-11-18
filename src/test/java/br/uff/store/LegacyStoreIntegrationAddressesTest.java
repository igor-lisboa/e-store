package br.uff.store;

import br.uff.store.core.dtos.AddressDTO;
import br.uff.store.core.dtos.CartDTO;
import br.uff.store.core.exceptions.StoreException;
import org.junit.Test;
import static org.junit.Assert.*;

public class LegacyStoreIntegrationAddressesTest extends StoreIntegrationFixture {
    @Test
    public void createdAddressRetainsItsOwnerAndDeliveryFields() throws Exception {
        AddressDTO address = createAddress(2);
        AddressDTO saved = addresses.findAddressById(address.getId());
        assertEquals(Integer.valueOf(2), saved.getUserId());
        assertEquals("12 Main Street", saved.getStreetAddress());
        assertEquals(Integer.valueOf(12345), saved.getPostalCode());
        assertEquals("Test City", saved.getCity());
        assertEquals("Test Country", saved.getCountry());
        assertTrue(addresses.listUserAddresses(1).isEmpty());
    }

    @Test
    public void ownerCanUpdateAddressDetails() throws Exception {
        AddressDTO address = createAddress(2);
        address.setStreetAddress("34 New Street");
        address.setCity("New City");
        addresses.updateAddressById(address.getId(), address);
        AddressDTO updated = addresses.findAddressById(address.getId());
        assertEquals("34 New Street", updated.getStreetAddress());
        assertEquals("New City", updated.getCity());
    }

    @Test
    public void anotherAccountCannotDeleteAnAddressButOwnerCan() throws Exception {
        AddressDTO address = createAddress(2);
        assertThrows(StoreException.class, () -> addresses.deleteAddressById(address.getId(), 1));
        assertEquals(1, addresses.listUserAddresses(2).size());
        addresses.deleteAddressById(address.getId(), 2);
        assertTrue(addresses.listUserAddresses(2).isEmpty());
    }

    @Test
    public void addressUsedByAnOrderCannotBeDeleted() throws Exception {
        AddressDTO address = createAddress(2);
        CartDTO cart = createCart(2, 1, 1);
        sales.recordSale(2, cart.getId(), address.getId());
        StoreException failure = assertThrows(StoreException.class,
                () -> addresses.deleteAddressById(address.getId(), 2));
        assertEquals("An address used for an order cannot be deleted.", failure.getMessage());
        assertEquals(address.getId(), sales.listUserSales(2).get(0).getAddressId());
        assertEquals("12 Main Street", addresses.findAddressById(address.getId()).getStreetAddress());
    }
}
