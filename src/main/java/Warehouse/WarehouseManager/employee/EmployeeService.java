package Warehouse.WarehouseManager.employee;

import Warehouse.WarehouseManager.email.EmailService;
import Warehouse.WarehouseManager.enums.Role;
import Warehouse.WarehouseManager.exception.*;
import Warehouse.WarehouseManager.security.ChangePasswordDto;
import Warehouse.WarehouseManager.security.LoginResponseDto;
import Warehouse.WarehouseManager.security.SecurityService;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final SecurityService securityService;
    private final EmailService emailService;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, final SecurityService securityService, final EmailService emailService) {
        this.employeeRepository = employeeRepository;
        this.securityService = securityService;
        this.emailService = emailService;
    }

    public Employee getEmployeeByUsername(String username) {
        return employeeRepository.findEmployeeByUsername(username).orElseThrow(() -> new EmployeeNotExistsException(username));
    }

    public EmployeeDto getEmployeeDtoByUsername(String username) {
        return getEmployeeByUsername(username).toEmployeeDto();
    }

    public EmployeeDto getEmployeeDtoById(Long id) {
        return employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotExistsException("id="+id)).toEmployeeDto();
    }

    private List<Employee> getEmployees() {
        return employeeRepository.findAll();
    }

    public List<EmployeeDto> getEmployeeDtoList() {
        List<EmployeeDto> employeeDtoList = new ArrayList<>();
        getEmployees().forEach(employee -> employeeDtoList.add(employee.toEmployeeDto()));
        return employeeDtoList;
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

        emailService.sendActivationEmail(employeeDto);

        return employee.toEmployeeDto();
    }

    @Transactional
    public LoginResponseDto employeeLogin(EmployeeDto employeeDto) {
        if (employeeDto.username().isBlank() || employeeDto.password().isBlank()) {
            throw new EmptyDataException();
        }
        EmployeeDto encodedEmployeeDto = getEmployeeDtoByUsername(employeeDto.username());
        checkActivate(encodedEmployeeDto.isActive(), true);

        if (securityService.checkPassword(employeeDto.password(), encodedEmployeeDto.password())) {
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
            throw new WrongCredentialsException("Wrong username or password");
        }
    }

    @Transactional
    public LoginResponseDto refreshAccessToken(String bearerRefreshToken) {
        String refreshToken = securityService.getAccessTokenFromBearer(bearerRefreshToken);
        Employee employee = getEmployeeByUsername(securityService.verifyToken(refreshToken).getClaim("username").asString());

        if (!employee.getRefreshToken().equals(refreshToken)) {
            throw new WrongCredentialsException("Authorization token not provided");
        } else if (employee.getRefreshTokenExpirationDate().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Refresh token expired. Please log in again");
        } else {
            String newAccessToken = securityService.generateAccessToken(employee.toEmployeeDto());
            employee.setAccessToken(newAccessToken);
            employee.setAccessTokenExpirationDate(securityService.getTokenExpirationDate(newAccessToken));
            employeeRepository.save(employee);
            return new LoginResponseDto(newAccessToken, securityService.getAccessTokenFromBearer(bearerRefreshToken));
        }
    }

    @Transactional
    public void activateAccount(String activationToken) {
        DecodedJWT decodedToken = securityService.verifyToken(activationToken);
        if (decodedToken.getExpiresAt().before(Date.from(Instant.now()))) {
            throw new TokenExpiredException("Activation token expired");
        }
        Employee employee = getEmployeeByUsername(decodedToken.getSubject());
        checkActivate(employee.isActive(), false);

        employee.setActive(true);
        employeeRepository.save(employee);
        checkActivate(getEmployeeDtoByUsername(decodedToken.getSubject()).isActive(), true);
    }

    private void checkActivate(boolean isActive, boolean shouldBeActive) {
        if (isActive && !shouldBeActive) {
            throw new EmployeeActivationException("Account has already been activated");
        } else if (!isActive && shouldBeActive) {
            throw new EmployeeActivationException("Account inactive");
        }
    }

    @Transactional
    public void changePassword(ChangePasswordDto changePasswordDto) {
        if (changePasswordDto.username().isBlank() || changePasswordDto.newPassword().isBlank() || changePasswordDto.oldPassword().isBlank()) {
            throw new EmptyDataException();
        }
        Employee encodedEmployee = getEmployeeByUsername(changePasswordDto.username());
        if (securityService.checkPassword(changePasswordDto.oldPassword(), encodedEmployee.getPassword())) {
            encodedEmployee.setPassword(securityService.encodePassword(changePasswordDto.newPassword()));
            employeeRepository.save(encodedEmployee);
        } else throw new WrongCredentialsException("Incorrect password");
    }

    public Role getEmployeeRoleByEmployeeId(long employeeId){
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(()-> new EmployeeNotExistsException("Employee"));
        return employee.getRole();
    }
}
