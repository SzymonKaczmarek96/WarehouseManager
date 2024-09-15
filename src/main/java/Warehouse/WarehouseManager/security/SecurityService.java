package Warehouse.WarehouseManager.security;

import Warehouse.WarehouseManager.employee.EmployeeDto;
import Warehouse.WarehouseManager.exception.EmptyDataException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SecurityService {

    private final JWTUtil jwtUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public SecurityService(final JWTUtil jwtUtil, final BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.jwtUtil = jwtUtil;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public String encodePassword(String password) {
        return bCryptPasswordEncoder.encode(password);
    }

    public boolean checkPassword(String password, String  encodedPassword) {
        if (password.isBlank() || encodedPassword.isBlank()) {
            throw new EmptyDataException();
        }
        return bCryptPasswordEncoder.matches(password, encodedPassword);
    }

    public String generateActivationToken(EmployeeDto employeeDto) {
        return jwtUtil.createActivationToken(employeeDto);
    }

    public String generateAccessToken(EmployeeDto employeeDto) {
        return jwtUtil.createAccessToken(employeeDto);
    }

    public String generateRefreshToken(EmployeeDto employeeDto) {
        return jwtUtil.createRefreshToken(employeeDto);
    }

    public LocalDateTime getTokenExpirationDate(String token) {
        return jwtUtil.getExpirationDate(verifyToken(token));
    }

    public DecodedJWT verifyToken(String token) {
        return jwtUtil.verifyToken(token);
    }
}
