package Warehouse.WarehouseManager.security;

public record ChangePasswordDto(String username, String oldPassword, String newPassword) {
}
