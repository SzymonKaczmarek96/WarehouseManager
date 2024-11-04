package Warehouse.WarehouseManager.email;

import Warehouse.WarehouseManager.employee.EmployeeDto;
import Warehouse.WarehouseManager.employee.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
public class EmailController {
    private final EmailService emailService;
    private final EmployeeService employeeService;

    @Autowired
    public EmailController(final EmailService emailService, final EmployeeService employeeService) {
        this.emailService = emailService;
        this.employeeService = employeeService;
    }

    @PostMapping("/activate")
    public ResponseEntity sendActivationEmail(@RequestBody EmployeeDto employeeDto) {
        emailService.sendActivationEmail(employeeDto);
        return new ResponseEntity(HttpStatus.OK);
    }
}
