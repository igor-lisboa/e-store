package br.uff.store.core.dtos;

import java.math.BigDecimal;

import java.util.Map;

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

    public ProductDTO(Map<String, Object> product) {
        this.setId(Integer.valueOf(String.valueOf(product.get("id"))));
        this.setName(String.valueOf(product.get("name")));
        this.setPrice(new BigDecimal(String.valueOf(product.get("price"))));
        this.setDescription(String.valueOf(product.get("description")));
        this.setImage(String.valueOf(product.get("image")));
        this.setCategory(String.valueOf(product.get("category")));
        this.setCategoryId(Integer.valueOf(String.valueOf(product.get("categoryId"))));
        this.setQuantity(Integer.valueOf(String.valueOf(product.get("quantity"))));
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
