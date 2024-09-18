package Warehouse.WarehouseManager.product;

import Warehouse.WarehouseManager.enums.ProductSize;

public record ProductDto(Long id,String name, ProductSize size) {
}
