package Warehouse.WarehouseManager.employee;

import Warehouse.WarehouseManager.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    private String accessToken;

    private String refreshToken;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime accessTokenExpirationDate;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime refreshTokenExpirationDate;


    public EmployeeDto toEmployeeDto() {
        return new EmployeeDto(id, username, email, password, isActive, role
                , accessToken, refreshToken, accessTokenExpirationDate, refreshTokenExpirationDate);
    }

}
