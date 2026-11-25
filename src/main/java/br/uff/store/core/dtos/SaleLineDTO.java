package br.uff.store.core.dtos;

import java.math.BigDecimal;

/** One purchased line as it was at checkout: later product edits or deletion never change it. */
public class SaleLineDTO extends BaseDTO {

    private Integer id;
    private Integer productId;
    private String productName;
    private String image;
    private BigDecimal unitPrice;
    private Integer quantity;

    public SaleLineDTO() {
    }

    public SaleLineDTO(Integer id, Integer productId, String productName, String image, BigDecimal unitPrice,
            Integer quantity) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.image = image;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public Integer getId() {
        return id;
    }

    /** Null when the product was deleted after the purchase. */
    public Integer getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    /** Null when the product was deleted after the purchase. */
    public String getImage() {
        return image;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
