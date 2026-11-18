package br.uff.store.core.dtos;

import java.util.Map;

public class RoleDTO {

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

    public RoleDTO() {
    }

    public RoleDTO(Map<String, Object> role) {
        this.setId(Integer.valueOf(String.valueOf(role.get("id"))));
        this.setName(String.valueOf(role.get("name")));
    }
}
