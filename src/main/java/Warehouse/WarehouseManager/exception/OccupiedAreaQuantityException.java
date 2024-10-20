package Warehouse.WarehouseManager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class OccupiedAreaQuantityException extends RuntimeException{
    public OccupiedAreaQuantityException() {
        super("Occupied area have to greater then -1 and less than capacity");
    }
}
