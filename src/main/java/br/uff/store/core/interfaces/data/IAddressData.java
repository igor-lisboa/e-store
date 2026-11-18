package br.uff.store.core.interfaces.data;

import java.util.List;

import br.uff.store.core.dtos.AddressDTO;
import br.uff.store.core.exceptions.StoreException;

public interface IAddressData {

    public AddressDTO findAddressById(Integer id) throws StoreException;

    public void deleteAddressById(Integer id) throws StoreException;

    public void updateAddressById(Integer id, AddressDTO address) throws StoreException;

    public Boolean addressBelongsToUser(Integer id, Integer userId) throws StoreException;

    public void insertAddress(AddressDTO address) throws StoreException;

    public List<AddressDTO> listUserAddresses(Integer userId) throws StoreException;

    public Boolean addressUsedInSale(Integer addressId) throws StoreException;
}
