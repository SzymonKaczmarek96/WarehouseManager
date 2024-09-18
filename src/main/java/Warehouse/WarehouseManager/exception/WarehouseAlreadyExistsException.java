package Warehouse.WarehouseManager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class WarehouseAlreadyExistsException extends RuntimeException {
    public WarehouseAlreadyExistsException() {
        super("Warehouse with this name already exists");
    }
}
