package br.uff.store.core.services;

import java.util.List;

import br.uff.store.core.dtos.AddressDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IAddressData;
import br.uff.store.core.interfaces.services.IAddressService;

public class AddressService implements IAddressService {

    private final IAddressData addressData;

    public AddressService(IAddressData addressData) {
        if (addressData == null) {
            throw new IllegalArgumentException("Address data dependency is required.");
        }
        this.addressData = addressData;
    }

    @Override
    public AddressDTO findAddressById(Integer id) throws StoreException {
        return addressData.findAddressById(id);
    }

    @Override
    public void deleteAddressById(Integer id, Integer userId) throws StoreException {
        InputRules.positiveId(id, "Address ID");
        InputRules.positiveId(userId, "User ID");
        if (!Boolean.TRUE.equals(addressData.addressBelongsToUser(id, userId))) {
            throw new StoreException("The address does not belong to this account.");
        }
        if (Boolean.TRUE.equals(addressData.addressUsedInSale(id))) {
            throw new StoreException("An address used for an order cannot be deleted.");
        }
        addressData.deleteAddressById(id);
    }

    @Override
    public AddressDTO updateAddressById(Integer id, AddressDTO address) throws StoreException {
        InputRules.positiveId(id, "Address ID");
        validateAddress(address);
        if (!Boolean.TRUE.equals(addressData.addressBelongsToUser(id, address.getUserId()))) {
            throw new StoreException("The address does not belong to this account.");
        }
        addressData.updateAddressById(id, address);
        return this.findAddressById(id);
    }

    @Override
    public void insertAddress(AddressDTO address) throws StoreException {
        validateAddress(address);
        addressData.insertAddress(address);
    }

    @Override
    public List<AddressDTO> listUserAddresses(Integer userId) throws StoreException {
        return addressData.listUserAddresses(userId);
    }

    @Override
    public Boolean addressBelongsToUser(Integer id, Integer userId) throws StoreException {
        return addressData.addressBelongsToUser(id, userId);
    }

    private void validateAddress(AddressDTO address) throws StoreException {
        if (address == null) {
            throw new StoreException("Address details are required.");
        }
        InputRules.positiveId(address.getUserId(), "User ID");
        InputRules.requiredText(address.getName(), 255, "Address name");
        InputRules.requiredText(address.getStreetAddress(), 255, "Street address");
        InputRules.requiredText(address.getCity(), 255, "City");
        InputRules.requiredText(address.getState(), 255, "State");
        InputRules.requiredText(address.getCountry(), 255, "Country");
        if (address.getPostalCode() == null || address.getPostalCode() < 0) {
            throw new StoreException("A non-negative postal code is required.");
        }
    }
}
