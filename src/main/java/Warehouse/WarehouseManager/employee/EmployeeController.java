package Warehouse.WarehouseManager.employee;

import Warehouse.WarehouseManager.security.LoginResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {
    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<EmployeeDto> getEmployeeByUsername(@PathVariable String username) {
        EmployeeDto employeeDto = employeeService.getEmployeeDtoByUsername(username);
        return ResponseEntity.ok(employeeDto);
    }

    @PostMapping("/registration")
    public ResponseEntity<EmployeeDto> registerEmployee(@RequestBody EmployeeDto employeeDto) {
        EmployeeDto employee = employeeService.employeeRegistration(employeeDto);
        return ResponseEntity.ok().body(employee);
    }

    @PatchMapping("/login")
    public ResponseEntity<LoginResponseDto> loginEmployee(@RequestBody EmployeeDto employeeDto) {
        return ResponseEntity.ok(employeeService.employeeLogin(employeeDto));
    }

    @PatchMapping("/activate/{token}")
    public ResponseEntity activateAccount(@PathVariable("token") String activationToken) {
        employeeService.activateAccount(activationToken);
        return ResponseEntity.ok("200 OK");
    }

}
