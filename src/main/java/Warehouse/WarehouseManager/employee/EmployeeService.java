package Warehouse.WarehouseManager.employee;

import Warehouse.WarehouseManager.exception.*;
import Warehouse.WarehouseManager.security.LoginResponseDto;
import Warehouse.WarehouseManager.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

    private EmployeeRepository employeeRepository;
    private final SecurityService securityService;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, final SecurityService securityService) {
        this.employeeRepository = employeeRepository;
        this.securityService = securityService;
    }

    public Employee getEmployeeByUsername(String username) {
        return employeeRepository.findEmployeeByUsername(username).orElseThrow(() -> new EmployeeNotExistsException(username));
    }

    public EmployeeDto getEmployeeDtoByUsername(String username) {
        return getEmployeeByUsername(username).toEmployeeDto();
    }

    @Transactional
    public EmployeeDto employeeRegistration(EmployeeDto employeeDto) {
        if (employeeDto.username().isEmpty() || employeeDto.password().isEmpty() || employeeDto.email().isEmpty()) {
            throw new EmptyDataException();
        }
        if (employeeRepository.existsEmployeeByEmail(employeeDto.email())) {
            throw new EmailAlreadyExistsException();
        }
        if (employeeRepository.existsEmployeeByUsername(employeeDto.username())) {
            throw new UsernameAlreadyExistsException();
        }
        Employee employee = new Employee();
        employee.setUsername(employeeDto.username());
        employee.setPassword(securityService.encodePassword(employeeDto.password()));
        employee.setEmail(employeeDto.email());
        employee.setActive(false);
        employee.setRole(employeeDto.role());
        employeeRepository.save(employee);
        return employee.toEmployeeDto();
    }

    @Transactional
    public LoginResponseDto employeeLogin(EmployeeDto employeeDto) {
        if (employeeDto.username().isBlank() || employeeDto.password().isBlank()) {
            throw new EmptyDataException();
        }
        EmployeeDto encodedEmployeeDto = getEmployeeDtoByUsername(employeeDto.username());
        if (!encodedEmployeeDto.isActive()) {
            throw new InactiveEmployeeException("Account inactive");
        }

        if (securityService.checkPassword(employeeDto, encodedEmployeeDto)) {
            Employee employee = getEmployeeByUsername(employeeDto.username());
            String accessToken = securityService.generateAccessToken(encodedEmployeeDto);
            String refreshToken = securityService.generateRefreshToken(encodedEmployeeDto);
            employee.setAccessToken(accessToken);
            employee.setAccessTokenExpirationDate(securityService.getTokenExpirationDate(accessToken));
            employee.setRefreshToken(refreshToken);
            employee.setRefreshTokenExpirationDate(securityService.getTokenExpirationDate(refreshToken));
            employeeRepository.save(employee);
            return new LoginResponseDto(accessToken, refreshToken);
        } else {
            throw new WrongCredentialsException();
        }
    }
}
