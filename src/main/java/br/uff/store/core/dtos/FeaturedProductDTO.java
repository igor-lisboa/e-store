package br.uff.store.core.dtos;

import java.util.Map;

public class FeaturedProductDTO extends BaseDTO {

    private Integer id;
    private String name;
    private String description;
    private String image;

    public FeaturedProductDTO() {
    }

    public FeaturedProductDTO(Map<String, Object> product) {
        this.setId(Integer.valueOf(String.valueOf(product.get("id"))));
        this.setName(String.valueOf(product.get("name")));
        this.setDescription(String.valueOf(product.get("description")));
        this.setImage(String.valueOf(product.get("image")));
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
}
