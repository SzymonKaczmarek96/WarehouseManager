package Warehouse.WarehouseManager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class WarehouseCapacityExceededException extends RuntimeException{
    public WarehouseCapacityExceededException() {
        super("Warehouse capacity exceeded");
    }
}
