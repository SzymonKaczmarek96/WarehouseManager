package Warehouse.WarehouseManager.employee;

import Warehouse.WarehouseManager.exception.EmailAlreadyExistsException;
import Warehouse.WarehouseManager.exception.EmployeeNotExistsException;
import Warehouse.WarehouseManager.exception.EmptyDataException;
import Warehouse.WarehouseManager.exception.UsernameAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

    private EmployeeRepository employeeRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.employeeRepository = employeeRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }
    public EmployeeDto getEmployeeByUsername(String username){
        Employee employee = employeeRepository.findEmployeeByUsername(username)
                .orElseThrow(() -> new EmployeeNotExistsException(username));
        return employee.toEmployeeDto();
    }

    @Transactional
    public EmployeeDto employeeRegistration(EmployeeDto employeeDto){
        if(employeeDto.username().isEmpty() || employeeDto.password().isEmpty() || employeeDto.email().isEmpty()){
            throw new EmptyDataException();
        }
        if(employeeRepository.existsEmployeeByEmail(employeeDto.email())){
           throw new EmailAlreadyExistsException();
        }
        if(employeeRepository.existsEmployeeByUsername(employeeDto.username())){
            throw new UsernameAlreadyExistsException();
        }
        Employee employee = new Employee();
        employee.setId(employeeDto.id());
        employee.setUsername(employeeDto.username());
        employee.setPassword(bCryptPasswordEncoder.encode(employeeDto.password()));
        employee.setEmail(employeeDto.email());
        employee.setActive(false);
        employee.setRole(employeeDto.role());
        employeeRepository.save(employee);
        return employee.toEmployeeDto();
    }
}
