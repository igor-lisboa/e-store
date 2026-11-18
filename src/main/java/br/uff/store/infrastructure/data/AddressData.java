package br.uff.store.infrastructure.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.uff.store.core.dtos.AddressDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IAddressData;
import br.uff.store.infrastructure.database.MySQLDAO;

public class AddressData implements IAddressData {

    private final MySQLDAO mysqlDAO;

    public AddressData() {
        this.mysqlDAO = new MySQLDAO();
    }

    @Override
    public AddressDTO findAddressById(Integer id) throws StoreException {
        try {
            Object[] bind = {id};
            List<HashMap<String, Object>> result = this.mysqlDAO.query("SELECT id, name AS name, user_id AS userId, zipcode AS postalCode, address AS streetAddress, city AS city, state AS state, country AS country FROM address WHERE id=?", bind);
            return new AddressDTO(result.get(0));
        } catch (Exception e) {
            throw new StoreException("Unable to complete the address database operation.", e);
        }
    }

    @Override
    public void deleteAddressById(Integer id) throws StoreException {
        try {
            Object[] bind = {id};
            this.mysqlDAO.executeUpdate("DELETE FROM address WHERE id=?", bind);
        } catch (Exception e) {
            throw new StoreException("Unable to complete the address database operation.", e);
        }
    }

    @Override
    public void updateAddressById(Integer id, AddressDTO address) throws StoreException {
        try {
            Object[] bind = {address.getName(), address.getPostalCode(), address.getStreetAddress(), address.getCity(), address.getState(), address.getCountry(), id};
            this.mysqlDAO.executeUpdate("UPDATE address SET name=?, zipcode=?, address=?, city=?, state=?, country=? WHERE id=?", bind);
        } catch (Exception e) {
            throw new StoreException("Unable to complete the address database operation.", e);
        }
    }

    @Override
    public void insertAddress(AddressDTO address) throws StoreException {
        try {
            Object[] bind = {address.getName(), address.getUserId(), address.getPostalCode(), address.getStreetAddress(), address.getCity(), address.getState(), address.getCountry()};
            this.mysqlDAO.executeUpdate("INSERT INTO address (name,user_id,zipcode,address,city,state,country) VALUES (?,?,?,?,?,?,?)", bind);
        } catch (Exception e) {
            throw new StoreException("Unable to complete the address database operation.", e);
        }
    }

    @Override
    public List<AddressDTO> listUserAddresses(Integer userId) throws StoreException {
        try {
            Object[] bind = {userId};
            List<HashMap<String, Object>> rows = this.mysqlDAO.query("SELECT id, name AS name, user_id AS userId, zipcode AS postalCode, address AS streetAddress, city AS city, state AS state, country AS country FROM address WHERE user_id=?", bind);
            List<AddressDTO> mappedResults = new ArrayList<>();
            rows.forEach(address -> mappedResults.add(new AddressDTO(address)));
            return mappedResults;
        } catch (Exception e) {
            throw new StoreException("Unable to complete the address database operation.", e);
        }
    }

    @Override
    public Boolean addressBelongsToUser(Integer id, Integer userId) throws StoreException {
        try {
            Object[] bind = {id, userId};
            return Integer.valueOf(String.valueOf(this.mysqlDAO.scalar("count(*)", "address", "id=? AND user_id=?", bind))) > 0;
        } catch (Exception e) {
            throw new StoreException("Unable to complete the address database operation.", e);
        }
    }

    @Override
    public Boolean addressUsedInSale(Integer addressId) throws StoreException {
        try {
            Object[] bind = {addressId};
            return Integer.valueOf(String.valueOf(this.mysqlDAO.scalar("count(*)", "sales", "address_id=?", bind))) > 0;
        } catch (Exception e) {
            throw new StoreException("Unable to complete the address database operation.", e);
        }
    }
}
