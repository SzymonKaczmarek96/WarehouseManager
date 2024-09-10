package Warehouse.WarehouseManager.employee;

import Warehouse.WarehouseManager.enums.Role;

import java.time.LocalDateTime;
import java.util.Set;

public record EmployeeDto(long id,String username, String email, String password, boolean isActive
        , Role role, String accessToken, String refreshToken
        , LocalDateTime accessTokenExpirationDate,LocalDateTime refreshTokenExpirationDate) {
}