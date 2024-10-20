package Warehouse.WarehouseManager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class StockQuantityException extends RuntimeException{
    public StockQuantityException() {
        super("Quantity must be greater than zero and can't be higher than current stock");
    }
}
