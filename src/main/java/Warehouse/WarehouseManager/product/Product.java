package Warehouse.WarehouseManager.product;

import Warehouse.WarehouseManager.enums.ProductSize;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.engine.jdbc.Size;
import org.springframework.stereotype.Service;

import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity

public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Long id;

    @Column(unique = true, nullable = false, name = "product_name")
    @Setter
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "product_size")
    private ProductSize size;

    public ProductDto toProductDto(){
        return new ProductDto(id,name,size);
    }
    public void setSize(ProductSize size) {
        EnumSet<ProductSize> productSizeList = EnumSet.allOf(ProductSize.class);
        if(!productSizeList.contains(size)){
            throw new IllegalArgumentException("Size of the product is incorrect");
        }
        this.size = size;
    }
}
