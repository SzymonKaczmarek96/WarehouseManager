package Warehouse.WarehouseManager.productstored;

import Warehouse.WarehouseManager.enums.Status;
import Warehouse.WarehouseManager.product.Product;
import Warehouse.WarehouseManager.warehouse.Warehouse;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.engine.jdbc.Size;
import org.springframework.jmx.export.annotation.ManagedOperation;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class ProductStored {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String lotNumber;

    //TODO: migrate deliveryNumber to new class DeliveryNote
    @Column(nullable = false)
    private String deliveryNumber;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Long quantity;
}
