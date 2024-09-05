package Warehouse.WarehouseManager.enums;

public enum ProductSize {
    SMALL(1),
    MEDIUM(10),
    BIG(100),
    PALETTE(10000);

    private final int value;

    ProductSize(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
