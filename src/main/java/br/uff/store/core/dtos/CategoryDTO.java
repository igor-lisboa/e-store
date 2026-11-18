package br.uff.store.core.dtos;

import java.util.Map;

public class CategoryDTO {

    private Integer id;
    private String name;

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CategoryDTO() {
    }

    public CategoryDTO(Map<String, Object> category) {
        this.setId(Integer.valueOf(String.valueOf(category.get("id"))));
        this.setName(String.valueOf(category.get("name")));
    }
}
