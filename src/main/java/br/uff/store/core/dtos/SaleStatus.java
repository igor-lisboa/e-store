package br.uff.store.core.dtos;

/** Order lifecycle values stored in sales.status. Only PLACED orders can be cancelled. */
public final class SaleStatus {
    public static final String PLACED = "PLACED";
    public static final String SHIPPED = "SHIPPED";
    public static final String CANCELLED = "CANCELLED";

    private SaleStatus() { }
}
