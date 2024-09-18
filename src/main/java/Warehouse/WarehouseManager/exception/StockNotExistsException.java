package Warehouse.WarehouseManager.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class StockNotExistsException extends RuntimeException{
    public StockNotExistsException() {
        super("Stock doesn't exists");
    }
}
