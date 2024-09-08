package Warehouse.WarehouseManager.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmptyDataException extends RuntimeException{
    public EmptyDataException() {
        super("part of the data is empty");
    }
}
