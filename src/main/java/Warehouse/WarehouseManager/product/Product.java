package Warehouse.WarehouseManager.product;

import Warehouse.WarehouseManager.enums.ProductSize;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.engine.jdbc.Size;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, name = "product_name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "product_size")
    private ProductSize size;
}
