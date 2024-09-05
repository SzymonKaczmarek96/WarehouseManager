package Warehouse.WarehouseManager.warehouse;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
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
    private int capacity;

    @Column(nullable = false)
    private int occupiedArea;
}
