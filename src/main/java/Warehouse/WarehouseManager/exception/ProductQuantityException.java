package Warehouse.WarehouseManager.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ProductQuantityException extends RuntimeException {
    public ProductQuantityException() {
        super("Can't delete product with stock more than 0");
    }
}
