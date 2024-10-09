package Warehouse.WarehouseManager.warehouse;


import Warehouse.WarehouseManager.employee.EmployeeDto;
import Warehouse.WarehouseManager.employee.EmployeeService;
import Warehouse.WarehouseManager.enums.*;
import Warehouse.WarehouseManager.exception.*;
import Warehouse.WarehouseManager.product.ProductRepository;
import Warehouse.WarehouseManager.security.SecurityService;
import Warehouse.WarehouseManager.stock.Stock;
import Warehouse.WarehouseManager.stock.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        warehouse.setWarehouseTasks(new WarehouseTasks(new ArrayList<>(Arrays.asList(warehouseTask))));
        warehouseRepository.save(warehouse);
        return warehouseTask;
    }

    @Transactional
    public WarehouseTask changeApproval(long warehouseId,long warehouseTaskId,String bearerToken){
        if(checkEmployeeAccess(bearerToken, WarehouseSystemOperation.MODIFY, Resource.WAREHOUSE_OPERATION)){
            throw new AccessDeniedException();
        }
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow(WarehouseNotFoundException::new);
        WarehouseTask warehouseTask = warehouse.getWarehouseTasks().getWarehouseTaskList()
                .stream().filter(task -> warehouseTaskId == task.getId()).findFirst().get();
        warehouseTask.setApprovalStatus(ApprovalStatus.APPROVED);
        warehouse.setWarehouseTasks(new WarehouseTasks(new ArrayList<>(List.of(warehouseTask))));
        warehouseRepository.save(warehouse);
        return warehouseTask;
    }

    @Transactional
    public WarehouseTask completeWarehouseTask(WarehouseTask warehouseTask, long warehouseId){
        if(warehouseTask.getApprovalStatus().equals(ApprovalStatus.NOT_APPROVED)){
            throw new TaskNotApprovedException();
        }
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow(WarehouseNotFoundException::new);
        validateOfStatus(warehouseTask.getStatus());
        validateOfProduct(warehouseTask.getProductId());
        quantityChange(warehouseTask.getStatus(),warehouseTask.getQuantity(),warehouseTask.getProductId());
        warehouseTask.setCompletionStatus(CompletionStatus.DONE);
        return warehouse.getWarehouseTasks().getWarehouseTaskList().stream()
                .filter(task -> warehouseTask.getId() == task.getId()).findFirst().get();
    }

    @Transactional
    public WarehouseTasks getWarehouseTaskListByApproved(long warehouseId,ApprovalStatus approvalStatus){
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow(WarehouseNotFoundException::new);
        if(approvalStatus.equals(ApprovalStatus.APPROVED)){
            return new WarehouseTasks(warehouse.getWarehouseTasks().getWarehouseTaskList()
                    .stream().filter(warehouseTask -> warehouseTask.getApprovalStatus()
                            .equals(ApprovalStatus.APPROVED)).toList());
        }
        return new WarehouseTasks(warehouse.getWarehouseTasks().getWarehouseTaskList()
                .stream().filter(warehouseTask -> warehouseTask.getApprovalStatus()
                        .equals(ApprovalStatus.NOT_APPROVED)).toList());
    }

    @Transactional
    public WarehouseTasks getWarehouseTaskListByCompletionStatus(long warehouseId,CompletionStatus completionStatus){
        Warehouse warehouse = warehouseRepository.findById(warehouseId).orElseThrow(WarehouseNotFoundException::new);
        if(completionStatus.equals(CompletionStatus.NOT_DONE)){
            return new WarehouseTasks(warehouse.getWarehouseTasks().getWarehouseTaskList()
                    .stream().filter(warehouseTask -> warehouseTask.getCompletionStatus()
                            .equals(CompletionStatus.NOT_DONE)).toList());
        }
        return new WarehouseTasks(warehouse.getWarehouseTasks().getWarehouseTaskList()
                .stream().filter(warehouseTask -> warehouseTask.getCompletionStatus()
                        .equals(CompletionStatus.DONE)).toList());
    }

    //TODO wonder about capacity with task done example method warehouseCapacityChange(), add delete task, test, controller
    //TODO wonder about method into a modify the whole task
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

    private boolean checkEmployeeAccess(String bearerToken,WarehouseSystemOperation warehouseSystemOperation, Resource resource){
        EmployeeDto employeeDto = securityService.getEmployeeDtoFromBearerToken(bearerToken);
        if(!employeeDto.role().hasAccessTo(warehouseSystemOperation, resource)){
            return true;
        }
        return false;
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

    private void checkQuantityStockBeforeReleaseWithWarehouse(Status status,long stockId,long quantity){
        if(status.equals(Status.RELEASE_AREA)){
            Stock stock = stockRepository.findById(stockId).orElseThrow(StockNotExistsException::new);
            if(stock.getQuantity() < quantity){
                throw new StockQuantityException();
            }
        }
    }
}
