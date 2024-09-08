package Warehouse.WarehouseManager.employee;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {
    private EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<EmployeeDto> getEmployeeByUsername(@PathVariable String username){
        EmployeeDto employeeDto = employeeService.getEmployeeByUsername(username);
        return ResponseEntity.ok(employeeDto);
    }

    @PostMapping("/registration")
    public ResponseEntity<EmployeeDto> registerEmployee(@RequestBody EmployeeDto employeeDto){
        EmployeeDto employee = employeeService.employeeRegistration(employeeDto);
        return ResponseEntity.ok().body(employee);
    }
}
