package br.uff.store.core.dtos;

public class PaginationDTO<T> {

    private Integer pageNumber;
    private T items;
    private Integer lastPage;

    public PaginationDTO() {
    }

    public PaginationDTO(
            Integer pageNumber,
            T items,
            Integer lastPage
    ) {
        this.setPageNumber(pageNumber);
        this.setItems(items);
        this.setLastPage(lastPage);
    }

    public void setItems(T items) {
        this.items = items;
    }

    public T getItems() {
        return items;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public Integer getLastPage() {
        return lastPage;
    }

    public void setLastPage(Integer lastPage) {
        this.lastPage = lastPage;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }
}
