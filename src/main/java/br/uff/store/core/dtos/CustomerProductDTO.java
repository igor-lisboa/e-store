package br.uff.store.core.dtos;

import java.util.Map;

public class CustomerProductDTO extends BaseDTO {

    private Integer id;
    private String name;
    private Integer quantity;
    private String description;
    private String image;
    private Double price;
    private String category;
    private Boolean favorite;
    private Integer userRating;
    private Integer ratingCount;
    private Integer ratingSum;
    private Integer ratingCount1;
    private Integer ratingCount2;
    private Integer ratingCount3;
    private Integer ratingCount4;
    private Integer ratingCount5;

    private Double ratingBar5;
    private Double ratingBar4;
    private Double ratingBar3;
    private Double ratingBar2;
    private Double ratingBar1;
    private Double averageRating;

    public CustomerProductDTO() {
    }

    public CustomerProductDTO(Map<String, Object> product) {
        this.setId(Integer.valueOf(String.valueOf(product.get("id"))));
        this.setName(String.valueOf(product.get("name")));
        this.setPrice(Double.valueOf(String.valueOf(product.get("price"))));
        this.setDescription(String.valueOf(product.get("description")));
        this.setImage(String.valueOf(product.get("image")));
        this.setCategory(String.valueOf(product.get("category")));
        this.setQuantity(Integer.valueOf(String.valueOf(product.get("quantity"))));
        this.setFavorite(Integer.valueOf(String.valueOf(product.get("favorite"))) > 0);
        this.setUserRating(DtoValues.optionalInteger(product.get("userRating")));
        this.setRatingCount(Integer.valueOf(String.valueOf(product.get("ratingCount"))));
        this.setRatingSum(Integer.valueOf(String.valueOf(product.get("ratingSum"))));
        this.setRatingCount1(Integer.valueOf(String.valueOf(product.get("ratingCount1"))));
        this.setRatingCount2(Integer.valueOf(String.valueOf(product.get("ratingCount2"))));
        this.setRatingCount3(Integer.valueOf(String.valueOf(product.get("ratingCount3"))));
        this.setRatingCount4(Integer.valueOf(String.valueOf(product.get("ratingCount4"))));
        this.setRatingCount5(Integer.valueOf(String.valueOf(product.get("ratingCount5"))));

        if (this.getRatingCount() > 0) {
            this.setAverageRating(Double.valueOf(this.ratingSum) / Double.valueOf(this.ratingCount));
            this.setRatingBar1(Double.valueOf((this.ratingCount1 * 100.0) / this.ratingCount));
            this.setRatingBar2(Double.valueOf((this.ratingCount2 * 100.0) / this.ratingCount));
            this.setRatingBar3(Double.valueOf((this.ratingCount3 * 100.0) / this.ratingCount));
            this.setRatingBar4(Double.valueOf((this.ratingCount4 * 100.0) / this.ratingCount));
            this.setRatingBar5(Double.valueOf((this.ratingCount5 * 100.0) / this.ratingCount));
        } else {
            this.setAverageRating(0.0);
            this.setRatingBar1(0.0);
            this.setRatingBar2(0.0);
            this.setRatingBar3(0.0);
            this.setRatingBar4(0.0);
            this.setRatingBar5(0.0);
        }
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Double getAverageRating() {
        return this.averageRating;
    }

    public void setRatingBar1(Double ratingBar1) {
        this.ratingBar1 = ratingBar1;
    }

    public void setRatingBar2(Double ratingBar2) {
        this.ratingBar2 = ratingBar2;
    }

    public void setRatingBar3(Double ratingBar3) {
        this.ratingBar3 = ratingBar3;
    }

    public void setRatingBar4(Double ratingBar4) {
        this.ratingBar4 = ratingBar4;
    }

    public void setRatingBar5(Double ratingBar5) {
        this.ratingBar5 = ratingBar5;
    }

    public Double getRatingBar1() {
        return ratingBar1;
    }

    public Double getRatingBar2() {
        return ratingBar2;
    }

    public Double getRatingBar3() {
        return ratingBar3;
    }

    public Double getRatingBar4() {
        return ratingBar4;
    }

    public Double getRatingBar5() {
        return ratingBar5;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setUserRating(Integer userRating) {
        this.userRating = userRating;
    }

    public Integer getUserRating() {
        return userRating;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setFavorite(Boolean favorite) {
        this.favorite = favorite;
    }

    public Boolean getFavorite() {
        return favorite;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getPrice() {
        return price;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setRatingCount(Integer ratingCount) {
        this.ratingCount = ratingCount;
    }

    public Integer getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount1(Integer ratingCount1) {
        this.ratingCount1 = ratingCount1;
    }

    public Integer getRatingCount1() {
        return ratingCount1;
    }

    public void setRatingCount2(Integer ratingCount2) {
        this.ratingCount2 = ratingCount2;
    }

    public Integer getRatingCount2() {
        return ratingCount2;
    }

    public void setRatingCount3(Integer ratingCount3) {
        this.ratingCount3 = ratingCount3;
    }

    public Integer getRatingCount3() {
        return ratingCount3;
    }

    public void setRatingCount5(Integer ratingCount5) {
        this.ratingCount5 = ratingCount5;
    }

    public Integer getRatingCount4() {
        return ratingCount4;
    }

    public void setRatingCount4(Integer ratingCount4) {
        this.ratingCount4 = ratingCount4;
    }

    public Integer getRatingSum() {
        return ratingSum;
    }

    public void setRatingSum(Integer ratingSum) {
        this.ratingSum = ratingSum;
    }

    public Integer getRatingCount5() {
        return ratingCount5;
    }
}
