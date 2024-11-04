package Warehouse.WarehouseManager.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class WarehouseTaskNotExistsException extends RuntimeException {
    public WarehouseTaskNotExistsException() {
        super("Task doesn't exist");
    }
}
