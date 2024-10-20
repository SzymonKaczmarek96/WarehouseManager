package Warehouse.WarehouseManager.stock;

import Warehouse.WarehouseManager.product.Product;
import Warehouse.WarehouseManager.warehouse.Warehouse;

public record StockDto(long id, Product product, Warehouse warehouse, long quantity) {
}
