package Warehouse.WarehouseManager.enums;

public enum ProductSize {
    SMALL(1L),
    MEDIUM(10L),
    BIG(100L),
    PALLET(10000L);

    private final Long value;

    ProductSize(final Long value) {
        this.value = value;
    }

    public Long getValue() {
        return value;
    }
}
