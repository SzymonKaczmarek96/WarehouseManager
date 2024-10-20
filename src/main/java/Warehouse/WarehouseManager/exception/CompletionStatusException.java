package Warehouse.WarehouseManager.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CompletionStatusException extends RuntimeException {
    public CompletionStatusException() {
        super("The task that has been performed cannot be changed");
    }
}
