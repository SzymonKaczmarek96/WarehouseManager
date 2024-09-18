package Warehouse.WarehouseManager.warehouse;

import Warehouse.WarehouseManager.employee.EmployeeDto;
import Warehouse.WarehouseManager.employee.EmployeeService;
import Warehouse.WarehouseManager.enums.ProductSize;
import Warehouse.WarehouseManager.enums.Resource;
import Warehouse.WarehouseManager.enums.WarehouseSystemOperation;
import Warehouse.WarehouseManager.exception.*;
import Warehouse.WarehouseManager.security.SecurityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final SecurityService securityService;
    private final EmployeeService employeeService;

    public WarehouseService(final WarehouseRepository warehouseRepository, final SecurityService securityService, final EmployeeService employeeService) {
        this.warehouseRepository = warehouseRepository;
        this.securityService = securityService;
        this.employeeService = employeeService;
    }

    private List<Warehouse> getWarehouses() {
        return warehouseRepository.findAll();
    }

    public List<WarehouseDto> getWarehouseDtoList() {
        return warehouseRepository.findAll().stream()
                .map(Warehouse::toWarehouseDto)
                .collect(Collectors.toList());
    }

    private Warehouse getWarehouseById(Long id) {
        return warehouseRepository.findById(id).orElseThrow(() -> new WarehouseNotFoundException());
    }

    public WarehouseDto getWarehouseDtoById(Long id) {
        return getWarehouseById(id).toWarehouseDto();
    }

    @Transactional
    public WarehouseDto addWarehouse(WarehouseDto warehouseDto, String bearerToken) {
        EmployeeDto employeeDto = securityService.getEmployeeDtoFromBearerToken(bearerToken);
        if (!employeeDto.role().hasAccessTo(WarehouseSystemOperation.ADD, Resource.WAREHOUSE)) {
            throw new AccessDeniedException();
        }

        if (warehouseDto.name().isBlank() || warehouseDto.capacity() == null) {
            throw new EmptyDataException();
        } else if (warehouseRepository.existsByName(warehouseDto.name())) {
            throw new WarehouseAlreadyExistsException();
        } else if (!isCorrectWarehouseCapacity(warehouseDto.capacity())) {
            throw new IllegalDataException("Warehouse must be able to store between 500 and 2500 pallets (range " + 500 * ProductSize.PALLET.getValue() + " - " + 2500 * ProductSize.PALLET.getValue() + ")");
        }

        Warehouse warehouse = new Warehouse();
        warehouse.setName(warehouseDto.name());
        warehouse.setCapacity(warehouseDto.capacity());
        warehouse.setOccupiedArea(0L);

        return warehouseRepository.save(warehouse).toWarehouseDto();
    }

    @Transactional
    public WarehouseDto modifyWarehouse(WarehouseDto warehouseDto, String bearerToken) {
        EmployeeDto employeeDto = getEmployeeDtoFromAccessToken(securityService.getAccessTokenFromBearer(bearerToken));
        if (!employeeDto.role().hasAccessTo(WarehouseSystemOperation.MODIFY, Resource.WAREHOUSE)) {
            throw new AccessDeniedException();
        }

        if (warehouseDto.id() == null) throw new EmptyDataException();
        Warehouse currentWarehouse = getWarehouseById(warehouseDto.id());
        if (!warehouseDto.name().isBlank()) {
            currentWarehouse.setName(warehouseDto.name());
        }
        if (warehouseDto.capacity() != null && isCorrectWarehouseCapacity(warehouseDto.capacity())) {
            currentWarehouse.setCapacity(warehouseDto.capacity());
        } else if (warehouseDto.capacity() != null && !isCorrectWarehouseCapacity(warehouseDto.capacity())) {
            throw new IllegalDataException("Warehouse must be able to store between 500 and 2500 pallets (range " + 500 * ProductSize.PALLET.getValue() + " - " + 2500 * ProductSize.PALLET.getValue() + ")");

        }
        return warehouseRepository.save(currentWarehouse).toWarehouseDto();
    }

    @Transactional
    public void deleteWarehouse(Long id, String bearerToken) {
        EmployeeDto employeeDto = getEmployeeDtoFromAccessToken(securityService.getAccessTokenFromBearer(bearerToken));

        if (!employeeDto.role().hasAccessTo(WarehouseSystemOperation.REMOVAL, Resource.WAREHOUSE)) {
            throw new AccessDeniedException();
        }

        if (warehouseRepository.existsById(id)) {
            warehouseRepository.deleteById(id);
        } else throw new WarehouseNotFoundException();
    }

    private boolean isCorrectWarehouseCapacity(Long capacity) {
        return capacity >= 500 * ProductSize.PALLET.getValue() && capacity <= 2500 * ProductSize.PALLET.getValue();
    }

    private EmployeeDto getEmployeeDtoFromAccessToken(String accessToken) {
        return employeeService.getEmployeeDtoById(Long.valueOf(securityService.verifyToken(accessToken).getSubject()));
    }


}
