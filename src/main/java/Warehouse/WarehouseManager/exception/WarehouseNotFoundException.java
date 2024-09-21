package Warehouse.WarehouseManager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class WarehouseNotFoundException extends RuntimeException {
    public WarehouseNotFoundException() {
        super("Warehouse not found");
    }
}