package br.uff.store.core.enums;

public enum ProductCategory {
    PLAYSTATION(1, "Playstation"),
    XBOX(2, "Xbox"),
    NINTENDO(3, "Nintendo"),
    PLAYSTATION_ACCESSORIES(4, "Playstation > Accessories"),
    PLAYSTATION_CONSOLES(5, "Playstation > Consoles"),
    PLAYSTATION_GAMES(6, "Playstation > Games"),
    XBOX_ACCESSORIES(7, "Xbox > Accessories"),
    XBOX_CONSOLES(8, "Xbox > Consoles"),
    XBOX_GAMES(9, "Xbox > Games"),
    NINTENDO_ACCESSORIES(10, "Nintendo > Accessories"),
    NINTENDO_CONSOLES(11, "Nintendo > Consoles"),
    NINTENDO_GAMES(12, "Nintendo > Games");

    private Integer id;
    private String name;

    ProductCategory(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Integer getId() {
        return id;
    }
}
