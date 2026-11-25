package br.uff.store.core.dtos;

import java.math.BigDecimal;

import java.util.Date;
import java.util.List;


public class SaleDTO extends BaseDTO {

    private Integer id;
    private Integer cartId;
    private BigDecimal totalPrice;
    private Date createdAt;
    private Integer addressId;
    private Integer userId;
    private List<SaleLineDTO> lines;
    private String status = SaleStatus.PLACED;
    private Date cancelledAt;
    private String paymentProvider;
    private String paymentStatus;
    private String deliveryMethod;
    private String trackingCode;
    private AddressDTO address;

    public SaleDTO() {
    }

    /** Payment vendor name recorded at checkout, for example "demo". */
    public String getPaymentProvider() {
        return paymentProvider;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public String getTrackingCode() {
        return trackingCode;
    }

    public AddressDTO getAddress() {
        return address;
    }

    public void setAddress(AddressDTO address) {
        this.address = address;
    }

    public void setLines(List<SaleLineDTO> lines) {
        this.lines = lines;
    }

    public List<SaleLineDTO> getLines() {
        return lines;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCancelledAt(Date cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public void setPaymentProvider(String paymentProvider) {
        this.paymentProvider = paymentProvider;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public void setTrackingCode(String trackingCode) {
        this.trackingCode = trackingCode;
    }

    public Date getCancelledAt() {
        return cancelledAt;
    }

    public boolean isCancellable() {
        return SaleStatus.PLACED.equals(status);
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
