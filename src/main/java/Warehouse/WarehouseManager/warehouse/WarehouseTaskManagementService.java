package Warehouse.WarehouseManager.warehouse;


import Warehouse.WarehouseManager.employee.EmployeeDto;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class WarehouseTaskManagementService {
    private  WarehouseRepository warehouseRepository;
    private  SecurityService securityService;
    private  EmployeeService employeeService;
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

    public WarehouseTasks getWarehouseTasksList(Long id){
        return warehouseRepository.findById(id).get().getWarehouseTasks();
    }

    @Transactional
    public WarehouseTask createWarehouseTask(WarehouseTask warehouseTask,long warehouseId){
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow(WarehouseNotFoundException::new);
        validateOfProduct(warehouseTask.getProductId());
        validateOfStatus(warehouseTask.getStatus());
        checkQuantityStockBeforeReleaseWithWarehouse(warehouseTask.getStatus(), warehouseTask.getProductId(), warehouseTask.getQuantity());
        checkWarehouseCapacity(warehouse,warehouseTask);
        warehouse.setWarehouseTasks(addTaskIntoList(warehouse.getWarehouseTasks(),warehouseTask));
        warehouseRepository.save(warehouse);
        return warehouseTask;
    }

    @Transactional
    public WarehouseTask changeApproval(long warehouseId,long employeeId,long warehouseTaskId){
        securityService.checkEmployeeAccess(employeeService.getEmployeeRoleByEmployeeId(employeeId)
                ,WarehouseSystemOperation.MODIFY,Resource.WAREHOUSE_OPERATION);
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow(WarehouseNotFoundException::new);
        WarehouseTask warehouseTask = warehouse.getWarehouseTasks().getWarehouseTaskList()
                .stream().filter(task -> warehouseTaskId == task.getId()).findFirst().orElseThrow(WarehouseTaskNotExistsException::new);
        warehouseTask.setApprovalStatus(ApprovalStatus.APPROVED);
        warehouse.setWarehouseTasks(new WarehouseTasks(new ArrayList<>(List.of(warehouseTask))));
        warehouseRepository.save(warehouse);
        return warehouseTask;
    }

    @Transactional
    //not working
    public WarehouseTask completeWarehouseTask(WarehouseTask warehouseTask, long warehouseId){
        if(warehouseTask.getApprovalStatus().equals(ApprovalStatus.NOT_APPROVED)){
            throw new TaskNotApprovedException();
        }
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow(WarehouseNotFoundException::new);
        validateOfStatus(warehouseTask.getStatus());
        validateOfProduct(warehouseTask.getProductId());
        quantityChange(warehouseTask.getStatus(),warehouseTask.getQuantity(),warehouseTask.getProductId());
        warehouseTask.setApprovalStatus(ApprovalStatus.DONE);
        return warehouse.getWarehouseTasks().getWarehouseTaskList().stream()
                .filter(task -> warehouseTask.getId() == task.getId()).findFirst().orElseThrow(WarehouseTaskNotExistsException::new);
    }


    public WarehouseTasks getWarehouseTaskListByApproved(long warehouseId,ApprovalStatus approvalStatus){
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow(WarehouseNotFoundException::new);
            return new WarehouseTasks(warehouse.getWarehouseTasks().getWarehouseTaskList()
                    .stream().filter(warehouseTask -> warehouseTask.getApprovalStatus()
                            .equals(approvalStatus)).toList());
    }


    @Transactional
    public WarehouseTask updateWarehouseTaskInformation(long warehouseId,WarehouseTask warehouseTask){
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow(WarehouseNotFoundException::new);
        WarehouseTask foundTask = findTheTask(warehouse.getWarehouseTasks(),warehouseTask);
        validateOfProduct(warehouseTask.getProductId());
        validateOfStatus(warehouseTask.getStatus());
        checkQuantityStockBeforeReleaseWithWarehouse(warehouseTask.getStatus(), warehouseTask.getProductId(), warehouseTask.getQuantity());
        foundTask.setProductId(warehouseTask.getProductId());
        foundTask.setQuantity(warehouseTask.getQuantity());
        foundTask.setApprovalStatus(ApprovalStatus.NOT_APPROVED);
        foundTask.setTaskCreatedAt(LocalDate.now());
        foundTask.setStatus(warehouseTask.getStatus());
        warehouseRepository.save(warehouse);
        return foundTask;
    }

    @Transactional
    public void deleteWarehouseTask(long warehouseId, WarehouseTask warehouseTask){
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow(WarehouseNotFoundException::new);
        warehouse.setWarehouseTasks(deleteTaskWithTaskList(warehouse.getWarehouseTasks(),warehouseTask));
        warehouseRepository.save(warehouse);
    }
    private WarehouseTasks addTaskIntoList(WarehouseTasks warehouseTasks, WarehouseTask warehouseTask){
        warehouseTasks.getWarehouseTaskList().add(warehouseTask);
        return warehouseTasks;
    }

    private WarehouseTasks deleteTaskWithTaskList(WarehouseTasks warehouseTasks, WarehouseTask warehouseTask){
        warehouseTasks.getWarehouseTaskList().removeIf(task -> task.getId() == warehouseTask.getId());
        return warehouseTasks;
    }

    private WarehouseTask findTheTask(WarehouseTasks warehouseTasks, WarehouseTask warehouseTask){
        return warehouseTasks.getWarehouseTaskList().stream()
                .filter(task -> task.getId() == warehouseTask.getId()).findFirst()
                .orElseThrow(WarehouseTaskNotExistsException::new);
    }


    //TODO wonder about capacity with task done example method warehouseCapacityChange()
    //TODO wonder about method into a modify the whole task - test
    @Transactional
    private void quantityChange(Status status, long quantity, long productId){
        Stock stock = stockRepository.findStockByProductId(productId).orElseThrow(StockNotExistsException::new);
        if(status.equals(Status.RELEASE_AREA)){
            stock.setQuantity(stock.getQuantity() - quantity);
            stockRepository.save(stock);
        }
        stock.setQuantity(stock.getQuantity() + quantity);
        stockRepository.save(stock);
    }

    private void checkWarehouseCapacity(Warehouse warehouse, WarehouseTask warehouseTask){
        long currentWarehouseCapacity = warehouse.getCapacity() - warehouse.getOccupiedArea();
        Product product = productRepository.findById(warehouseTask.getProductId())
                .orElseThrow(() -> new ProductNotExistsException("Product"));
        if(currentWarehouseCapacity < (product.getSize().getValue() * warehouseTask.getQuantity())){
            throw new WarehouseCapacityExceededException();
        }
    }


    private void validateOfProduct(long productId){
        if(!productRepository.existsById(productId)){
            throw new ProductNotExistsException("");
        }
        System.out.println("Product exists in repository");
    }

    private void validateOfStatus(Status status){
        if(status.equals(Status.RECEPTION_AREA) || status.equals(Status.RELEASE_AREA)){
            System.out.println("Correct status");
        }else {
            throw new IncorrectStatusException();
        }
    }

    private void checkQuantityStockBeforeReleaseWithWarehouse(Status status,long productId,long quantity){
        if(status.equals(Status.RELEASE_AREA)){
            Stock stock = stockRepository.findStockByProductId(productId).orElseThrow(StockNotExistsException::new);
            if(stock.getQuantity() < quantity){
                throw new StockQuantityException();
            }
        }
    }
}
