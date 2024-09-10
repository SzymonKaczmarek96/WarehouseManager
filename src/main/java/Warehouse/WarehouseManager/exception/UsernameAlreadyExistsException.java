package Warehouse.WarehouseManager.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException() {
        super("The username provided is busy");
    }
}
