package br.uff.store.core.dtos;

import java.math.BigDecimal;

import java.util.Map;

public class ProductListDTO extends BaseDTO {

    private Integer id;
    private String name;
    private BigDecimal price;
    private String image;
    private String category;

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
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

    public ProductListDTO() {
    }

    public ProductListDTO(Map<String, Object> product) {
        this.setId(Integer.valueOf(String.valueOf(product.get("id"))));
        this.setName(String.valueOf(product.get("name")));
        this.setPrice(new BigDecimal(String.valueOf(product.get("price"))));
        this.setImage(String.valueOf(product.get("image")));
        this.setCategory(String.valueOf(product.get("category")));
    }

    public ProductListDTO(
            Integer id,
            String name,
            BigDecimal price,
            String image,
            String category
    ) {
        this.setId(id);
        this.setName(name);
        this.setPrice(price);
        this.setImage(image);
        this.setCategory(category);
    }
}
