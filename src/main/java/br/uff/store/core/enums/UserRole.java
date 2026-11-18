package br.uff.store.core.enums;

public enum UserRole {
    ADM(1),
    CLIENTE(2);

    private Integer id;

    UserRole(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
