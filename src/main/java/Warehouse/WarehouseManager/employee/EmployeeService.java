package Warehouse.WarehouseManager.employee;

import Warehouse.WarehouseManager.email.EmailService;
import Warehouse.WarehouseManager.exception.*;
import Warehouse.WarehouseManager.security.LoginResponseDto;
import Warehouse.WarehouseManager.security.SecurityService;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
}
