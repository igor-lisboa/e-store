package br.uff.store.core.dtos;

import java.util.List;

public class ProductFilterDTO extends BaseDTO {

    private Integer pageNumber;
    private Integer pageSize;
    private String search;
    private Double minimumPrice;
    private Double maximumPrice;
    private Boolean favoritesOnly;
    private Integer userId;
    private Boolean newReleasesOnly;
    private List<String> categories;
    private List<String> subcategories;

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Boolean getFavoritesOnly() {
        return favoritesOnly;
    }

    public Boolean getNewReleasesOnly() {
        return newReleasesOnly;
    }

    public List<String> getCategories() {
        return categories;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public String getSearch() {
        return search;
    }

    public Double getMaximumPrice() {
        return maximumPrice;
    }

    public Double getMinimumPrice() {
        return minimumPrice;
    }

    public List<String> getSubcategories() {
        return subcategories;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setFavoritesOnly(Boolean favoritesOnly) {
        this.favoritesOnly = favoritesOnly;
    }

    public void setNewReleasesOnly(Boolean newReleasesOnly) {
        this.newReleasesOnly = newReleasesOnly;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public void setMaximumPrice(Double maximumPrice) {
        this.maximumPrice = maximumPrice;
    }

    public void setMinimumPrice(Double minimumPrice) {
        this.minimumPrice = minimumPrice;
    }

    public void setSubcategories(List<String> subcategories) {
        this.subcategories = subcategories;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
