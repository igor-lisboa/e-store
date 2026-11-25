package br.uff.store.core.dtos;

import java.math.BigDecimal;


public class ProductDTO extends BaseDTO {

    private Integer id;
    private String name;
    private BigDecimal price;
    private String description;
    private String image;
    private Integer categoryId;
    private Integer quantity;
    private String category;

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public ProductDTO() {
        this.setName("");
        this.setPrice(BigDecimal.ZERO);
        this.setDescription("");
    }

    public ProductDTO(Integer id, String name, BigDecimal price, String description, String image, Integer categoryId, Integer quantity) {
        this.setId(id);
        this.setName(name);
        this.setPrice(price);
        this.setDescription(description);
        this.setImage(image);
        this.setCategoryId(categoryId);
        this.setQuantity(quantity);
    }
}
