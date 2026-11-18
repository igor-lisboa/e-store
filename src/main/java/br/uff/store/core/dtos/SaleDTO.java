package br.uff.store.core.dtos;

import java.math.BigDecimal;

import java.util.Date;
import java.util.List;
import java.util.Map;


public class SaleDTO extends BaseDTO {

    private Integer id;
    private Integer cartId;
    private BigDecimal totalPrice;
    private Date createdAt;
    private Integer addressId;
    private Integer userId;
    private List<CartItemDTO> cartProducts;
    private AddressDTO address;

    public SaleDTO() {
    }

    public SaleDTO(Map<String, Object> sale) {
        this.setId(Integer.valueOf(String.valueOf(sale.get("id"))));
        this.setCartId(Integer.valueOf(String.valueOf(sale.get("cartId"))));
        this.setTotalPrice(new BigDecimal(String.valueOf(sale.get("totalPrice"))));
        this.setCreatedAt(DtoValues.date(sale.get("createdAt")));
        this.setAddressId(Integer.valueOf(String.valueOf(sale.get("addressId"))));
        this.setUserId(Integer.valueOf(String.valueOf(sale.get("userId"))));
    }

    public AddressDTO getAddress() {
        return address;
    }

    public void setAddress(AddressDTO address) {
        this.address = address;
    }

    public void setCartProducts(List<CartItemDTO> cartProducts) {
        this.cartProducts = cartProducts;
    }

    public List<CartItemDTO> getCartProducts() {
        return cartProducts;
    }

    public Integer getCartId() {
        return cartId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Integer getAddressId() {
        return addressId;
    }

    public Integer getId() {
        return id;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setCartId(Integer cartId) {
        this.cartId = cartId;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setAddressId(Integer addressId) {
        this.addressId = addressId;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
