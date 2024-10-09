package Warehouse.WarehouseManager.stock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock,Long> {

    @Query("SELECT s FROM Stock s WHERE s.product.name = :name")
    Optional<Stock> findStockByProductName(String name);

    Optional<Stock> findStockByProductId(long productId);

}
