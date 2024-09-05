package Warehouse.WarehouseManager.employee;

import Warehouse.WarehouseManager.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

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

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean isActive;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = Role.class)
//    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<Role> roles = Collections.emptySet();

    private String accessToken;

    private String refreshToken;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime accessTokenExpirationDate;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime refreshTokenExpirationDate;
}
