package Warehouse.WarehouseManager.unit;

import Warehouse.WarehouseManager.employee.EmployeeDto;
import Warehouse.WarehouseManager.employee.EmployeeRepository;
import Warehouse.WarehouseManager.exception.EmptyDataException;
import Warehouse.WarehouseManager.security.JWTUtil;
import Warehouse.WarehouseManager.security.SecurityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {
    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeDto employeeDto;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private SecurityService securityService;

    @Test
    public void shouldEncodePassword() {
        String rawPassword = "password";
        String myEncodedPassword = "xyz123!@#";
        when(bCryptPasswordEncoder.encode(rawPassword)).thenReturn(myEncodedPassword);

        String encodedPassword = securityService.encodePassword(rawPassword);

        assertNotNull(encodedPassword);
        assertEquals(encodedPassword, myEncodedPassword);
    }

    @Test
    public void shouldCheckPasswordReturnTrueWhenItMatches() {
        String rawPassword = "password";
        String myEncodedPassword = "xyz123!@#";
        when(bCryptPasswordEncoder.matches(rawPassword, myEncodedPassword)).thenReturn(true);

        boolean isMatch = securityService.checkPassword(rawPassword, myEncodedPassword);

        assertTrue(isMatch);
    }

    @Test
    public void shouldCheckPasswordReturnFalseWhenItDoesNotMatch() {
        String rawPassword = "password";
        String myEncodedPassword = "xyz123!@#";
        when(bCryptPasswordEncoder.matches(rawPassword, myEncodedPassword)).thenReturn(false);

        boolean isMatch = securityService.checkPassword(rawPassword, myEncodedPassword);

        assertFalse(isMatch);
    }

    @Test
    public void shouldCheckPasswordThrowAnExceptionWhenPasswordIsBlank() {
        String rawPassword = "password";
        String myEncodedPassword = "xyz123!@#";
        String blankPassword = "";

        assertThrows(EmptyDataException.class, () -> securityService.checkPassword(blankPassword, myEncodedPassword));
        assertThrows(EmptyDataException.class, () -> securityService.checkPassword(rawPassword, blankPassword));
    }

    @Test
    public void shouldReturnActivationToken() {
        String token = "123456asdfgh";

    }

}
