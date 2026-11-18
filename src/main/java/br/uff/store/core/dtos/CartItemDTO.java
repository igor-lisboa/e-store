package br.uff.store.core.dtos;

import java.math.BigDecimal;

import java.util.Map;

public class CartItemDTO extends BaseDTO {

    private Integer id;
    private Integer productId;
    private String name;
    private String description;
    private String image;
    private BigDecimal price;
    private Integer availableStock;
    private Integer quantity;
    private BigDecimal totalPrice;

    public CartItemDTO() {
    }

    public CartItemDTO(Map<String, Object> cartProduct) {
        this.setId(Integer.valueOf(String.valueOf(cartProduct.get("id"))));
        this.setProductId(Integer.valueOf(String.valueOf(cartProduct.get("productId"))));
        this.setName(String.valueOf(cartProduct.get("name")));
        this.setDescription(String.valueOf(cartProduct.get("description")));
        this.setImage(String.valueOf(cartProduct.get("image")));
        this.setPrice(new BigDecimal(String.valueOf(cartProduct.get("price"))));
        this.setAvailableStock(Integer.valueOf(String.valueOf(cartProduct.get("availableStock"))));
        this.setQuantity(Integer.valueOf(String.valueOf(cartProduct.get("quantity"))));
        this.setTotalPrice(new BigDecimal(String.valueOf(cartProduct.get("totalPrice"))));
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getProductId() {
        return productId;
    }

    public String getDescription() {
        return description;
    }

    public Integer getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setAvailableStock(Integer availableStock) {
        this.availableStock = availableStock;
    }
}
