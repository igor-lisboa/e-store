package br.uff.store.core.dtos;


public class ProductReviewListDTO extends BaseDTO {

    private String reviewer;
    private String title;
    private String rating;
    private String description;
    private String displayDate;
    private String shortDate;

    public ProductReviewListDTO() {
    }

    public void setShortDate(String shortDate) {
        this.shortDate = shortDate;
    }

    public String getShortDate() {
        return shortDate;
    }

    public void setDisplayDate(String displayDate) {
        this.displayDate = displayDate;
    }

    public String getDisplayDate() {
        return displayDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getRating() {
        return rating;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public String getReviewer() {
        return reviewer;
    }
}
