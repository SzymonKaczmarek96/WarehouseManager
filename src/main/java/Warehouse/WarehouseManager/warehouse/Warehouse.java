package Warehouse.WarehouseManager.warehouse;

import Warehouse.WarehouseManager.enums.Status;
import Warehouse.WarehouseManager.exception.OccupiedAreaQuantityException;
import Warehouse.WarehouseManager.exception.WarehouseCapacityExceededException;
import Warehouse.WarehouseManager.exception.WarehouseTaskNotExistsException;
import Warehouse.WarehouseManager.product.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Long id;

    @Column(unique = true, nullable = false, name = "warehouse_name")
    @Setter
    private String name;

    @Column(nullable = false)
    @Setter
    private Long capacity;

    @Column(nullable = false)
    private Long occupiedArea;

    @Setter
    @JdbcTypeCode(SqlTypes.JSON)
    private WarehouseTasks warehouseTasks;

    public WarehouseDto toWarehouseDto() {
        return new WarehouseDto(id, name, capacity, occupiedArea, warehouseTasks);
    }

    public void setOccupiedArea(Long occupiedArea) {
        if (occupiedArea < -1 || occupiedArea > capacity) {
            throw new OccupiedAreaQuantityException();
        }
        this.occupiedArea = occupiedArea;

    }
    public long calculateAvailableWarehouseCapacity() {
        return getCapacity() - getOccupiedArea();
    }
}
