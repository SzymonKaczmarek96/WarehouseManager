package Warehouse.WarehouseManager.warehouse;

import Warehouse.WarehouseManager.enums.ProductSize;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Entity
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, name = "warehouse_name")
    private String name;

    @Column(nullable = false)
    private Long capacity;

    @Column(nullable = false)
    private Long occupiedArea;

    public WarehouseDto toWarehouseDto() {
        return new WarehouseDto(id, name, capacity, occupiedArea);
    }
}
