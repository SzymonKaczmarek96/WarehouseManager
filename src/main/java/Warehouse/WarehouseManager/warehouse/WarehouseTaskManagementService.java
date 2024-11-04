package Warehouse.WarehouseManager.warehouse;

import Warehouse.WarehouseManager.employee.EmployeeService;
import Warehouse.WarehouseManager.enums.*;
import Warehouse.WarehouseManager.exception.*;
import Warehouse.WarehouseManager.product.Product;
import Warehouse.WarehouseManager.product.ProductRepository;
import Warehouse.WarehouseManager.security.SecurityService;
import Warehouse.WarehouseManager.stock.Stock;
import Warehouse.WarehouseManager.stock.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class WarehouseTaskManagementService {

    private WarehouseRepository warehouseRepository;
    private SecurityService securityService;
    private EmployeeService employeeService;
    private ProductRepository productRepository;
    private StockRepository stockRepository;

    @Autowired
    public WarehouseTaskManagementService(WarehouseRepository warehouseRepository, SecurityService securityService
            , EmployeeService employeeService, ProductRepository productRepository, StockRepository stockRepository) {
        this.warehouseRepository = warehouseRepository;
        this.securityService = securityService;
        this.employeeService = employeeService;
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
    }

    public WarehouseTasks getWarehouseTasksList(Long id) {
        return warehouseRepository.findById(id).get().getWarehouseTasks();
    }

    @Transactional
    public WarehouseTask createWarehouseTask(WarehouseTask warehouseTask, long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow(WarehouseNotFoundException::new);
        validateOfProduct(warehouseTask.getProductId());
        warehouseTask.validateWarehouseTaskStatus();
        checkQuantityStockBeforeReleaseWithWarehouse(warehouseTask.getStatus(), warehouseTask.getProductId(), warehouseTask.getQuantity());
        checkWarehouseCapacity(findProductById(warehouseTask.getProductId()),warehouseTask,warehouse);
        warehouse.setWarehouseTasks(addTaskIntoList(warehouse.getWarehouseTasks(), warehouseTask));
        warehouseRepository.save(warehouse);
        return warehouseTask;
    }

    @Transactional
    public WarehouseTask changeApproval(long warehouseId, long employeeId, long warehouseTaskId) {
        securityService.checkEmployeeAccess(employeeService.getEmployeeRoleByEmployeeId(employeeId)
                , WarehouseSystemOperation.MODIFY, Resource.WAREHOUSE_OPERATION);
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow(WarehouseNotFoundException::new);
        WarehouseTask warehouseTask = warehouse.getWarehouseTasks().getWarehouseTaskList()
                .stream().filter(task -> warehouseTaskId == task.getId()).findFirst().orElseThrow(WarehouseTaskNotExistsException::new);
        warehouseTask.approveTask();
        warehouse.setWarehouseTasks(new WarehouseTasks(new ArrayList<>(List.of(warehouseTask))));
        warehouseRepository.save(warehouse);
        return warehouseTask;
    }

    @Transactional
    public WarehouseTask completeWarehouseTask(WarehouseTask warehouseTask, long warehouseId) {
        if (warehouseTask.getApprovalStatus().equals(ApprovalStatus.NOT_APPROVED)) {
            throw new TaskNotApprovedException();
        }
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow(WarehouseNotFoundException::new);
        warehouseTask.validateWarehouseTaskStatus();
        validateOfProduct(warehouseTask.getProductId());
        quantityChange(warehouseTask.getStatus(), warehouseTask.getQuantity(), warehouseTask.getProductId());
        checkWarehouseCapacity(findProductById(warehouseTask.getProductId()),warehouseTask,warehouse);
        warehouse.setOccupiedArea(updateOccupiedAreaBasedOnStatus(warehouse.getOccupiedArea(),
                calculateTaskCapacity(warehouseTask.getProductId(), warehouseTask.getQuantity()), warehouseTask.getStatus()));
        warehouseTask.setApprovalStatus(ApprovalStatus.DONE);
        return warehouse.getWarehouseTasks().getWarehouseTaskList().stream()
                .filter(task -> warehouseTask.getId() == task.getId()).findFirst().orElseThrow(WarehouseTaskNotExistsException::new);
    }


    public WarehouseTasks getWarehouseTaskListByApproved(long warehouseId, ApprovalStatus approvalStatus) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow(WarehouseNotFoundException::new);
        return new WarehouseTasks(warehouse.getWarehouseTasks().getWarehouseTaskList()
                .stream().filter(warehouseTask -> warehouseTask.getApprovalStatus()
                        .equals(approvalStatus)).toList());
    }

    @Transactional
    public WarehouseTask modifyWarehouseTaskInformation(long warehouseId, WarehouseTask warehouseTask) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow(WarehouseNotFoundException::new);
        validateOfProduct(warehouseTask.getProductId());
        warehouseTask.validateWarehouseTaskStatus();
        checkQuantityStockBeforeReleaseWithWarehouse(warehouseTask.getStatus(), warehouseTask.getProductId(), warehouseTask.getQuantity());
        WarehouseTask foundTask = findTheTask(warehouse.getWarehouseTasks(), warehouseTask);
        foundTask.updateWith(warehouseTask);
        warehouseRepository.save(warehouse);
        return foundTask;
    }

    @Transactional
    public void deleteWarehouseTask(long warehouseId, WarehouseTask warehouseTask) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow(WarehouseNotFoundException::new);
        warehouse.setWarehouseTasks(deleteTaskWithTaskList(warehouse.getWarehouseTasks(), warehouseTask));
        warehouseRepository.save(warehouse);
    }

    private WarehouseTasks addTaskIntoList(WarehouseTasks warehouseTasks, WarehouseTask warehouseTask) {
        warehouseTasks.getWarehouseTaskList().add(warehouseTask);
        return warehouseTasks;
    }

    private WarehouseTasks deleteTaskWithTaskList(WarehouseTasks warehouseTasks, WarehouseTask warehouseTask) {
        warehouseTasks.getWarehouseTaskList().removeIf(task -> task.getId() == warehouseTask.getId());
        return warehouseTasks;
    }

    private WarehouseTask findTheTask(WarehouseTasks warehouseTasks, WarehouseTask warehouseTask) {
        return warehouseTasks.getWarehouseTaskList().stream()
                .filter(task -> task.getId() == warehouseTask.getId()).findFirst()
                .orElseThrow(WarehouseTaskNotExistsException::new);
    }

    @Transactional
    private void quantityChange(Status status, long quantity, long productId) {
        Stock stock = stockRepository.findStockByProductId(productId).orElseThrow(StockNotExistsException::new);
        if (status.equals(Status.RELEASE_AREA)) {
            stock.setQuantity(stock.getQuantity() - quantity);
            stockRepository.save(stock);
        }
        stock.setQuantity(stock.getQuantity() + quantity);
        stockRepository.save(stock);
    }

    private long updateOccupiedAreaBasedOnStatus(long currentCapacity, long taskCapacity, Status status) {
        if (status.equals(Status.RECEPTION_AREA)) {
            return currentCapacity + taskCapacity;
        }
        return currentCapacity - taskCapacity;
    }

    private long calculateTaskCapacity(long productId, long warehouseTaskQuantity) {
        return findProductById(productId).getSize().getValue() * warehouseTaskQuantity;
    }

    private void checkQuantityStockBeforeReleaseWithWarehouse(Status status, long productId, long quantity) {
        if (status.equals(Status.RELEASE_AREA)) {
            Stock stock = findStockByProductId(productId);
            if (stock.getQuantity() < quantity) {
                throw new StockQuantityException();
            }
        }
    }

    private Product findProductById(long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotExistsException("Product"));
    }

    private Stock findStockByProductId(long productId) {
        return stockRepository.findStockByProductId(productId).orElseThrow(StockNotExistsException::new);
    }

    private void validateOfProduct(long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotExistsException("Product");
        }
    }

    private void checkWarehouseCapacity(Product product, WarehouseTask warehouseTask, Warehouse warehouse) {
        if (warehouse.calculateAvailableWarehouseCapacity() < product.getSize().getValue() * warehouseTask.getQuantity()) {
            throw new WarehouseCapacityExceededException();
        }

    }
}
