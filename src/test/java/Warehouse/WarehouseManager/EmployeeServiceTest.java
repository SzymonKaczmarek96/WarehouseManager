package Warehouse.WarehouseManager;


import Warehouse.WarehouseManager.employee.Employee;
import Warehouse.WarehouseManager.employee.EmployeeDto;
import Warehouse.WarehouseManager.employee.EmployeeRepository;
import Warehouse.WarehouseManager.employee.EmployeeService;
import Warehouse.WarehouseManager.enums.Role;
import Warehouse.WarehouseManager.exception.EmailAlreadyExistsException;
import Warehouse.WarehouseManager.exception.EmployeeNotExistsException;
import Warehouse.WarehouseManager.exception.EmptyDataException;
import Warehouse.WarehouseManager.exception.UsernameAlreadyExistsException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    public void shouldFindEmployeeByUsername(){
        //given
        when(employeeRepository.findEmployeeByUsername("szymon")).thenReturn(Optional.of(createEmployeeTwo()));
        //when
        EmployeeDto employeeDto = employeeService.getEmployeeByUsername("szymon");
        //then
        assertNotNull(employeeDto);
        assertEquals("szymon@o2.pl",employeeDto.email());
        assertEquals("szymon",employeeDto.username());
    }

    @Test
    public void shouldThrowEmployeeNotExistsExceptionWhenEmployeeNotExists(){
        assertThrows(EmployeeNotExistsException.class,() -> employeeService.getEmployeeByUsername("szymon"));
    }


    @Test
    public void shouldSaveEmployeeToDB(){
        //given
        when(bCryptPasswordEncoder.encode(createEmployeeOne().getPassword())).thenReturn("encodedPassword");
        //when
        EmployeeDto employeeDto = employeeService.employeeRegistration(createEmployeeOne().toEmployeeDto());
        //then
        assertNotNull(employeeDto);
        verify(employeeRepository,times(1)).existsEmployeeByUsername(employeeDto.username());
        verify(employeeRepository,times(1)).save(any(Employee.class));
        verify(employeeRepository,times(1)).existsEmployeeByEmail(employeeDto.email());
    }

    @Test
    public void shouldThrowEmptyDataExceptionWhenUsernameIsEmpty(){
        //given
        Set<Role> roles = new HashSet<>();
        Employee employee3 = new Employee(
                2L,
                "",
                "szymon@interia.pl",
                "hashed_password",
                true,
                Role.ADMIN,
                "access_token_value",
                "refresh_token_value",
                LocalDateTime.of(2023, 9, 15, 12, 0),
                LocalDateTime.of(2024, 9, 15, 12, 0)
        );
        //when
        Assertions.assertThrows(EmptyDataException.class,()->employeeService.employeeRegistration(employee3.toEmployeeDto()));
    }

    @Test
    public void shouldThrowEmptyDataExceptionWhenPasswordIsEmpty(){
        //given
        Set<Role> roles = new HashSet<>();
        Employee employee3 = new Employee(
                2L,
                "szymon",
                "szymon@interia.pl",
                "",
                true,
                Role.ADMIN,
                "access_token_value",
                "refresh_token_value",
                LocalDateTime.of(2023, 9, 15, 12, 0),
                LocalDateTime.of(2024, 9, 15, 12, 0)
        );
        //when
        Assertions.assertThrows(EmptyDataException.class,()->employeeService.employeeRegistration(employee3.toEmployeeDto()));
    }

    @Test
    public void shouldThrowEmptyDataExceptionWhenEmailIsEmpty(){
        //given
        Set<Role> roles = new HashSet<>();
        Employee employee3 = new Employee(
                2L,
                "szymon",
                "",
                "1234",
                true,
                Role.ADMIN,
                "access_token_value",
                "refresh_token_value",
                LocalDateTime.of(2023, 9, 15, 12, 0),
                LocalDateTime.of(2024, 9, 15, 12, 0)
        );
        //when
        Assertions.assertThrows(EmptyDataException.class,()->employeeService.employeeRegistration(employee3.toEmployeeDto()));
    }

    @Test
    public void shouldThrowUsernameAlreadyExistsExceptionWhenUsernameExists(){
        //given
        when(employeeRepository.existsEmployeeByUsername("szymon")).thenReturn(true);
        //when
        Assertions.assertThrows(UsernameAlreadyExistsException.class, () -> employeeService.employeeRegistration(createEmployeeTwo().toEmployeeDto()));
    }

    @Test
    public void shouldThrowEmailAlreadyExistsExceptionWhenEmailExists(){
        //given
        when(employeeRepository.existsEmployeeByEmail("szymon@o2.pl")).thenReturn(true);
        //when
        assertThrows(EmailAlreadyExistsException.class, () -> employeeService.employeeRegistration(createEmployeeTwo().toEmployeeDto()));

    }

    private Employee createEmployeeOne() {

        Employee employee1 = new Employee(
                1L, // id
                "john", // username
                "johndoe@example.com", // email
                "hashed_password", // password
                true, // isActive
                Role.ADMIN, // roles
                "access_token_value", // accessToken
                "refresh_token_value", // refreshToken
                LocalDateTime.of(2023, 9, 15, 12, 0), // accessTokenExpirationDate
                LocalDateTime.of(2024, 9, 15, 12, 0) // refreshTokenExpirationDate
        );
        return employee1;
    }

    private Employee createEmployeeTwo() {
        Set<Role> roles = new HashSet<>();
        Employee employee2 = new Employee(
                2L, // id
                "szymon", // username
                "szymon@o2.pl", // email
                "hashed_password", // password
                true, // isActive
                Role.ADMIN, // roles
                "access_token_value", // accessToken
                "refresh_token_value", // refreshToken
                LocalDateTime.of(2023, 9, 15, 12, 0), // accessTokenExpirationDate
                LocalDateTime.of(2024, 9, 15, 12, 0) // refreshTokenExpirationDate
        );
        return employee2;
    }

    private Employee createEmployeeWithRepeatedUsername(){
        Set<Role> roles = new HashSet<>();
        Employee employee3 = new Employee(
                2L, // id
                "szymon", // username
                "szymon@interia.pl", // email
                "hashed_password", // password
                true, // isActive
                Role.ADMIN, // roles
                "access_token_value", // accessToken
                "refresh_token_value", // refreshToken
                LocalDateTime.of(2023, 9, 15, 12, 0), // accessTokenExpirationDate
                LocalDateTime.of(2024, 9, 15, 12, 0) // refreshTokenExpirationDate
        );
        return employee3;
    }
}

