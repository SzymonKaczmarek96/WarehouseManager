package Warehouse.WarehouseManager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class InactiveEmployeeException extends RuntimeException {
    public InactiveEmployeeException(String message) {
        super(message);
    }
}