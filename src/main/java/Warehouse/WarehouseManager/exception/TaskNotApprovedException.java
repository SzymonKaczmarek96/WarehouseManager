package Warehouse.WarehouseManager.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class TaskNotApprovedException extends RuntimeException{
    public TaskNotApprovedException() {
        System.out.println("Task not have approval");
    }
}
