package Warehouse.WarehouseManager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class EmployeeActivationException extends RuntimeException {
    public EmployeeActivationException(String message) {
        super(message);
    }
}