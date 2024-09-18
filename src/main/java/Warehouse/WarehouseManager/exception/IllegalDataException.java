package Warehouse.WarehouseManager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IllegalDataException extends RuntimeException {
    public IllegalDataException(final String message) {
        super(message);
    }
}
