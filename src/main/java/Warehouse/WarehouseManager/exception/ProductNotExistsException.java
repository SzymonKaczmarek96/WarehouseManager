package Warehouse.WarehouseManager.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductNotExistsException extends RuntimeException{
    public ProductNotExistsException(String productName) {
        super(productName + " doesn't exists in repository");
    }
}
