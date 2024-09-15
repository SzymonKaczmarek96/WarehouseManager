package Warehouse.WarehouseManager.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductAlreadyExistsException extends RuntimeException{

    public ProductAlreadyExistsException(String name){
        super("Product: " + name + " exists in DB");
    }
}
