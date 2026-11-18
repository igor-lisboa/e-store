package br.uff.store.core.dtos;

import java.util.Map;

public class AddressDTO extends BaseDTO {

    public AddressDTO() {
    }

    public AddressDTO(Map<String, Object> address) {
        this.setId(Integer.valueOf(String.valueOf(address.get("id"))));
        this.setUserId(Integer.valueOf(String.valueOf(address.get("userId"))));
        this.setName(String.valueOf(address.get("name")));
        this.setPostalCode(Integer.valueOf(String.valueOf(address.get("postalCode"))));
        this.setStreetAddress(String.valueOf(address.get("streetAddress")));
        this.setState(String.valueOf(address.get("state")));
        this.setCity(String.valueOf(address.get("city")));
        this.setCountry(String.valueOf(address.get("country")));
    }

    public AddressDTO(String name, Integer userId, Integer postalCode, String streetAddress, String city, String state, String country) {
        this.setUserId(userId);
        this.setName(name);
        this.setPostalCode(postalCode);
        this.setStreetAddress(streetAddress);
        this.setState(state);
        this.setCity(city);
        this.setCountry(country);
    }

    private Integer id;
    private String name;
    private Integer userId;
    private Integer postalCode;
    private String streetAddress;
    private String city;
    private String state;
    private String country;

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setPostalCode(Integer postalCode) {
        this.postalCode = postalCode;
    }

    public Integer getPostalCode() {
        return postalCode;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
