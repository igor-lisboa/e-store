package br.uff.store.core.dtos;

public class ProductReviewInputDTO extends BaseDTO {

    private Integer userId;
    private Integer productId;
    private Integer rating;
    private String description;
    private String title;

    public ProductReviewInputDTO() {
    }

    public ProductReviewInputDTO(
            Integer userId, Integer productId, Integer rating, String description, String title
    ) {
        this.setUserId(userId);
        this.setProductId(productId);
        this.setRating(rating);
        this.setDescription(description);
        this.setTitle(title);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
