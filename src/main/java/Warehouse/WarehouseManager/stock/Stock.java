package Warehouse.WarehouseManager.stock;

import Warehouse.WarehouseManager.exception.ProductQuantityException;
import Warehouse.WarehouseManager.exception.StockQuantityException;
import Warehouse.WarehouseManager.product.Product;
import Warehouse.WarehouseManager.warehouse.Warehouse;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @Setter
    private Product product;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", nullable = false)
    @Setter
    private Warehouse warehouse;

    private Long quantity;

    public StockDto toStockDto(){
        return new StockDto(id,product,warehouse,quantity);
    }

    public void setQuantity(Long quantity) {
        if(quantity < 0){
            throw new StockQuantityException();
        }
        this.quantity = quantity;
    }
}
