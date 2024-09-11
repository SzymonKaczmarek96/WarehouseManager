package Warehouse.WarehouseManager.security;

import Warehouse.WarehouseManager.employee.EmployeeDto;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JWTUtil {

    private final RSAKeyUtil rsaKeyUtil;
    private final Algorithm algorithm;
    private final JWTVerifier jwtVerifier;

    public JWTUtil(final RSAKeyUtil rsaKeyUtil) {
        this.rsaKeyUtil = rsaKeyUtil;
        this.algorithm = Algorithm.RSA256((RSAPublicKey) rsaKeyUtil.getPublicKey(), (RSAPrivateKey) rsaKeyUtil.getPrivateKey());
        this.jwtVerifier = JWT.require(this.algorithm).build();
    }

    public String createAccessToken(EmployeeDto employeeDto) {
        return JWT.create()
                .withSubject(String.valueOf(employeeDto.id()))
                .withClaim("username", employeeDto.username())
                .withClaim("email", employeeDto.email())
                .withIssuedAt(Date.from(Instant.now()))
                .withExpiresAt(Date.from(Instant.now().plusSeconds(1800)))
                .sign(algorithm);
    }

    public String createRefreshToken(EmployeeDto employeeDto) {
        return JWT.create()
                .withSubject(String.valueOf(employeeDto.id()))
                .withClaim("username", employeeDto.username())
                .withClaim("email", employeeDto.email())
                .withIssuedAt(Date.from(Instant.now()))
                .withExpiresAt(Date.from(Instant.now().plusSeconds(1209600)))
                .sign(algorithm);
    }

    public DecodedJWT verifyToken(String token) {
        return jwtVerifier.verify(token);
    }

    public LocalDateTime getExpirationDate(DecodedJWT decodedJWT) {
        return LocalDateTime.ofInstant(decodedJWT.getExpiresAt().toInstant(), ZoneId.systemDefault());
    }

}
