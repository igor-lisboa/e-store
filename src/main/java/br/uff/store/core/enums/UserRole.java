package br.uff.store.core.enums;

public enum UserRole {
    ADMIN(1),
    CUSTOMER(2);

    private Integer id;

    UserRole(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
