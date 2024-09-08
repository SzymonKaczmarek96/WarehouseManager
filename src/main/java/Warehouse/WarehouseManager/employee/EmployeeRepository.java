package Warehouse.WarehouseManager.employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee,Long> {
    Optional<Employee> findEmployeeByUsername(String username);

    boolean existsEmployeeByUsername(String username);

    boolean existsEmployeeByEmail(String email);
}
