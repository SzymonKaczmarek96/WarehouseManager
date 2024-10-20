package Warehouse.WarehouseManager;


import Warehouse.WarehouseManager.employee.Employee;
import Warehouse.WarehouseManager.employee.EmployeeService;
import Warehouse.WarehouseManager.enums.*;
import Warehouse.WarehouseManager.exception.*;
import Warehouse.WarehouseManager.product.Product;
import Warehouse.WarehouseManager.product.ProductRepository;
import Warehouse.WarehouseManager.security.SecurityService;
import Warehouse.WarehouseManager.stock.Stock;
import Warehouse.WarehouseManager.stock.StockRepository;
import Warehouse.WarehouseManager.warehouse.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WarehouseTaskManagementServiceTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private StockRepository stockRepository;
    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private SecurityService securityService;
    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private WarehouseTaskManagementService warehouseManagementTaskService;


    @Test
    public void shouldGetWarehouseTaskList(){
        //given
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithTaskList()));
        //when
        WarehouseTasks warehouseTasksList = warehouseManagementTaskService.getWarehouseTasksList(1L);
        assertNotNull(warehouseTasksList);
        assertEquals(3,warehouseTasksList.getWarehouseTaskList().size());
    }
    @Test
    public void shouldCreateWarehouseTask(){
        //given
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithTaskList()));
        when(productRepository.existsById(1L)).thenReturn(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(createProductWithProductSizeMedium()));
        when(stockRepository.findStockByProductId(1L)).thenReturn(Optional.of(createStockWithFiftyQuantity()));
        //when
        WarehouseTask task = warehouseManagementTaskService.createWarehouseTask
                (createWarehouseTaskWithNotApprovedStatus(),1L);
        //then
        assertEquals(1L,createWarehouseTaskWithNotApprovedStatus().getId());
        assertEquals(1L,createWarehouseTaskWithNotApprovedStatus().getProductId());
        assertEquals(50,createWarehouseTaskWithNotApprovedStatus().getQuantity());
        assertTrue(task.getApprovalStatus().equals(ApprovalStatus.NOT_APPROVED));
        assertEquals(Status.RELEASE_AREA,task.getStatus());
    }
    @Test
    public void shouldThrowWarehouseNotFoundExceptionWhenWarehouseNotExists(){
        //when then
        assertThrows(WarehouseNotFoundException.class, () -> warehouseManagementTaskService.createWarehouseTask(
                createWarehouseTaskWithNotApprovedStatus(),1L));
    }

    @Test
    public void shouldThrowProductNotExistsExceptionWhenProductNotExists(){
        //given
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithTaskList()));
        //when then
        assertThrows(ProductNotExistsException.class, () -> warehouseManagementTaskService.createWarehouseTask(
                createWarehouseTaskWithNotApprovedStatus(),1L));
    }

    @Test
    public void shouldThrowStockQuantityExceptionWhenStockIsLessThenRelease(){
        //given

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithTaskList()));
        when(productRepository.existsById(1L)).thenReturn(true);
        when(stockRepository.findStockByProductId(1L)).thenReturn(Optional.of(createStockWithFiftyQuantity()));
        //when then
        assertThrows(StockQuantityException.class,() -> warehouseManagementTaskService.createWarehouseTask(
                createWarehouseTaskWithSixtyQuantity(),1L));
    }

    @Test
    public void shouldThrowIncorrectStatusExceptionWhenStatusIsNotCorrect(){
        //given
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithOccupiedArea()));
        when(productRepository.existsById(1L)).thenReturn(true);
        //when then
        assertThrows(IncorrectStatusException.class,() -> warehouseManagementTaskService.createWarehouseTask
                (createWarehouseTaskWithShippedStatusAndNotApproved(),1L));
    }

    @Test
    public void shouldChangeApproval(){
        //given
        when(employeeService.getEmployeeRoleByEmployeeId(1L)).thenReturn(createEmployee().getRole());
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithTaskList()));
        //when
        WarehouseTask task = warehouseManagementTaskService.changeApproval(1L,1L,1L);
        //then
        assertTrue(task.getApprovalStatus().equals(ApprovalStatus.APPROVED));
    }

    @Test
    public void shouldThrowAccessDeniedExceptionWhenEmployeeLacksModifyAccess(){
        //given
        when(employeeService.getEmployeeRoleByEmployeeId(1L)).thenReturn(createEmployee().getRole());
        doThrow(new AccessDeniedException()).when(securityService)
                .checkEmployeeAccess(createEmployee().getRole(), WarehouseSystemOperation.MODIFY, Resource.WAREHOUSE_OPERATION);
        //when then
        assertThrows(AccessDeniedException.class, () ->
                warehouseManagementTaskService.changeApproval(1L,1L,1L));
    }

    @Test
    public void shouldThrowWarehouseNotFoundExceptionWhenChangingApprovalWithInvalidWarehouseId(){
        //given
        when(employeeService.getEmployeeRoleByEmployeeId(1L)).thenReturn(createEmployee().getRole());
        //when then
        assertThrows(WarehouseNotFoundException.class,()-> warehouseManagementTaskService.changeApproval(1L,1L,1L));
    }

    @Test
    public void shouldCompleteWarehouseOperationsWhenStatusIsReceptionArea(){
        //given
        Warehouse warehouse = createWarehouseWithTaskList();
        when(productRepository.findById(1L)).thenReturn(Optional.of(createProductWithProductSizeMedium()));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(productRepository.existsById(1L)).thenReturn(true);
        when(stockRepository.findStockByProductId(1L)).thenReturn(Optional.of(createStockWithFiftyQuantity()));
        //when
        WarehouseTask task = warehouseManagementTaskService.completeWarehouseTask(createWarehouseTaskWithStatusReceptionArea(),1L);
        //given
        assertEquals(ApprovalStatus.APPROVED,task.getApprovalStatus());
        assertEquals(500,warehouse.getOccupiedArea());
    }

    @Test
    public void shouldCompleteWarehouseOperationsWhenStatusIsReleaseArea(){
        //given
        Warehouse warehouse = createWarehouseWithOccupiedArea();
        when(productRepository.findById(1L)).thenReturn(Optional.of(createProductWithProductSizeMedium()));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(productRepository.existsById(1L)).thenReturn(true);
        when(stockRepository.findStockByProductId(1L)).thenReturn(Optional.of(createStockWithFiftyQuantity()));
        //when
        WarehouseTask task = warehouseManagementTaskService.completeWarehouseTask(createWarehouseTaskWithStatusReleaseArea(),1L);
        //then
        assertEquals(ApprovalStatus.APPROVED,task.getApprovalStatus());
        assertEquals(500,warehouse.getOccupiedArea());
    }

    @Test
    public void shouldThrowTaskNotApprovedExceptionWhenTaskIsNotApproved(){
        //when then
        assertThrows(TaskNotApprovedException.class,()-> warehouseManagementTaskService.completeWarehouseTask
                (createWarehouseTaskWithNotApprovedStatus(),1L));
    }

    @Test
    public void shouldThrowIncorrectStatusExceptionWhenCompleteTaskHasNotCorrectStatus(){
        //given
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.APPROVED, LocalDate.now(),LocalDate.now(),
                Status.SHIPPED);;
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithTaskList()));
        //when then
        assertThrows(IncorrectStatusException.class,() -> warehouseManagementTaskService.completeWarehouseTask
                (createWarehouseTaskWithShippedStatusAndApproved(),1L));
    }

    @Test
    public void shouldThrowWarehouseNotFoundExceptionWhenCompleteTaskWithInvalidWarehouseId() {
        //when then
        assertThrows(WarehouseNotFoundException.class,()-> warehouseManagementTaskService.completeWarehouseTask
                (createWarehouseTaskWithShippedStatusAndApproved(),1L));
    }

    @Test
    public void shouldThrowProductNotExistsExceptionWhenCompleteTaskHasNotExistingProduct(){
        //given
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithTaskList()));
        //when then
        assertThrows(ProductNotExistsException.class, () -> warehouseManagementTaskService.createWarehouseTask(
                createWarehouseTaskWithSixtyQuantity(),1L));
    }


    @Test
    public void shouldThrowStockNotExistsExceptionWhenCompleteTaskHasNotExistingStock(){
        //given
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithTaskList()));
        when(productRepository.existsById(1L)).thenReturn(true);
        //when then
        assertThrows(StockNotExistsException.class, () -> warehouseManagementTaskService.createWarehouseTask(
                createWarehouseTaskWithSixtyQuantity(),1L));
    }

    @Test
    public void shouldFindTaskListByApprovedStatus(){
        //given
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithTaskList()));
        //when
        WarehouseTasks warehouseTaskList = warehouseManagementTaskService.getWarehouseTaskListByApproved(1L,ApprovalStatus.APPROVED);
        //then
        assertNotNull(warehouseTaskList);
        assertEquals(2,warehouseTaskList.getWarehouseTaskList().size());
    }

    @Test
    public void shouldFindTaskListByNotApprovedStatus(){
        //given
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithTaskList()));
        //when
        WarehouseTasks warehouseTaskList = warehouseManagementTaskService.getWarehouseTaskListByApproved(1L,ApprovalStatus.NOT_APPROVED);
        //then
        assertNotNull(warehouseTaskList);
        assertEquals(1,warehouseTaskList.getWarehouseTaskList().size());
    }

    @Test
    public void shouldThrowWarehouseNotFoundExceptionWhenFindTaskByApprovedStatusWithInvalidWarehouseId(){
        //when then
        assertThrows(WarehouseNotFoundException.class, () -> warehouseManagementTaskService.getWarehouseTaskListByApproved(1L,ApprovalStatus.APPROVED));
    }

    @Test
    public void shouldFindTaskListByApprovalStatusIsApproved(){
        //given
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithTaskList()));
        //when
        WarehouseTasks warehouseTaskList = warehouseManagementTaskService.getWarehouseTaskListByApproved(1L,ApprovalStatus.APPROVED);
        //then
        assertNotNull(warehouseTaskList);
        assertEquals(2,warehouseTaskList.getWarehouseTaskList().size());
    }

    @Test
    public void shouldFindTaskListByApprovalStatusIsNotApproved(){
        //given
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithTaskList()));
        //when
        WarehouseTasks warehouseTaskList = warehouseManagementTaskService.getWarehouseTaskListByApproved(1L,ApprovalStatus.NOT_APPROVED);
        //then
        assertNotNull(warehouseTaskList);
        assertEquals(1,warehouseTaskList.getWarehouseTaskList().size());
    }

    @Test
    public void shouldThrowWarehouseCapacityExceededExceptionWhenCapacityIsNotEnough(){
            //given
            when(productRepository.findById(1L)).thenReturn(Optional.of(createProductWithProductSizeMedium()));
            when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithTaskList()));
            when(productRepository.existsById(1L)).thenReturn(true);
            //when given
            assertThrows(WarehouseCapacityExceededException.class,() -> warehouseManagementTaskService.createWarehouseTask
                    (createWarehouseTaskWithTooBigCapacityForWarehouse(),1L));
    }

    @Test
    public void shouldThrowProductNotExistsExceptionWhenProductNotExistsInWarehouse(){
        //given
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithTaskList()));
        //when then
        assertThrows(ProductNotExistsException.class,() -> warehouseManagementTaskService.createWarehouseTask(createWarehouseTaskWithStatusReceptionArea(),1L));
    }


    @Test
    public void shouldDeleteTask(){
        //given
        WarehouseTask warehouseTask1 = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.APPROVED, LocalDate.now(),LocalDate.now(),
                Status.RECEPTION_AREA);
        Warehouse warehouse = createWarehouseWithTaskList();
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        //when
        warehouseManagementTaskService.deleteWarehouseTask(1L,createWarehouseTaskWithStatusReceptionArea());
        //then
        assertEquals(2,warehouse.getWarehouseTasks().getWarehouseTaskList().size());
    }

    @Test
    public void shouldUpdateWarehouseTaskWhenStatusIsReceptionArea(){
        //given
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithTaskList()));
        when(productRepository.existsById(1L)).thenReturn(true);
        //when
        WarehouseTask warehouseTask = warehouseManagementTaskService.modifyWarehouseTaskInformation
                (1L,createWarehouseTaskWithStatusReceptionArea());
        //then
        assertEquals(1L,warehouseTask.getId());
        assertEquals(1L,warehouseTask.getProductId());
        assertEquals(50L,warehouseTask.getQuantity());
        assertEquals(ApprovalStatus.NOT_APPROVED,warehouseTask.getApprovalStatus());
        assertEquals(Status.RECEPTION_AREA,warehouseTask.getStatus());

    }

    @Test
    public void shouldUpdateWarehouseTaskWhenStatusIsReleaseArea(){
        //given
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithTaskList()));
        when(productRepository.existsById(1L)).thenReturn(true);
        when(stockRepository.findStockByProductId(1L)).thenReturn(Optional.of(createStockWithFiftyQuantity()));
        //when
        WarehouseTask warehouseTask = warehouseManagementTaskService.modifyWarehouseTaskInformation
                (1L,createWarehouseTaskWithStatusReleaseArea());
        //then
        assertEquals(1L,warehouseTask.getId());
        assertEquals(1L,warehouseTask.getProductId());
        assertEquals(50L,warehouseTask.getQuantity());
        assertEquals(ApprovalStatus.NOT_APPROVED,warehouseTask.getApprovalStatus());
        assertEquals(Status.RELEASE_AREA,warehouseTask.getStatus());
    }

    private List<WarehouseTask> createWarehouseTaskListForTest(){
        WarehouseTask warehouseTask1 = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.APPROVED, LocalDate.now(),LocalDate.now(),
                Status.RECEPTION_AREA);
        WarehouseTask warehouseTask2 = new WarehouseTask(
                2L, 1L, 50L, ApprovalStatus.APPROVED, LocalDate.now(),LocalDate.now(),
                Status.RECEPTION_AREA);
        WarehouseTask warehouseTask3 = new WarehouseTask(
                3L, 1L, 50L, ApprovalStatus.NOT_APPROVED, LocalDate.now(),LocalDate.now(),
                Status.RELEASE_AREA);
        List<WarehouseTask> warehouseTaskList = new ArrayList<>();
        warehouseTaskList.add(warehouseTask1);
        warehouseTaskList.add(warehouseTask2);
        warehouseTaskList.add(warehouseTask3);
        return warehouseTaskList;
    }

    private Warehouse createWarehouseWithTaskList(){
        return new Warehouse(1L,"M1",1000000L,0L
                ,new WarehouseTasks(createWarehouseTaskListForTest()));
    }

    private WarehouseTask createWarehouseTaskWithStatusReleaseArea(){
        return new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.APPROVED, LocalDate.now(),LocalDate.now(),
                Status.RELEASE_AREA);
    }

    private Warehouse createWarehouseWithOccupiedArea(){

        return new Warehouse(1L,"M1",1000000L,1000L
                ,new WarehouseTasks(List.of(createWarehouseTaskWithStatusReleaseArea())));
    }

    private WarehouseTask createWarehouseTaskWithNotApprovedStatus(){
        return new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.NOT_APPROVED, LocalDate.now(),LocalDate.now(),
                Status.RELEASE_AREA);
    }

    private WarehouseTask createWarehouseTaskWithTooBigCapacityForWarehouse(){
        return new WarehouseTask(
                1L, 1L, 1000000L, ApprovalStatus.APPROVED, LocalDate.now(),LocalDate.now(),
                Status.RECEPTION_AREA);
    }

    private WarehouseTask createWarehouseTaskWithStatusReceptionArea(){
        return new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.APPROVED, LocalDate.now(),LocalDate.now(),
                Status.RECEPTION_AREA);
    }

    private Stock createStockWithFiftyQuantity(){
        return new Stock(1L,createProductWithProductSizeMedium(),createWarehouseWithTaskList(),50L);
    }

    private Product createProductWithProductSizeMedium(){
        return new Product(1L,"Product", ProductSize.MEDIUM);
    }

    private WarehouseTask createWarehouseTaskWithSixtyQuantity(){
        return new WarehouseTask(
                1L, 1L, 60L, ApprovalStatus.NOT_APPROVED, LocalDate.now(),LocalDate.now(),
                Status.RELEASE_AREA);
    }

    private WarehouseTask createWarehouseTaskWithShippedStatusAndNotApproved(){
        return new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.NOT_APPROVED, LocalDate.now(),LocalDate.now(),
                Status.SHIPPED);
    }

    private WarehouseTask createWarehouseTaskWithShippedStatusAndApproved(){
        return new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.APPROVED, LocalDate.now(),LocalDate.now(),
                Status.SHIPPED);
    }

    private Employee createEmployee(){
        return new Employee(
                1L,
                "Szymon",
                "szymon@interia.pl",
                "hashed_password",
                true,
                Role.WAREHOUSE_OPERATOR,
                "access_token_value",
                "refresh_token_value",
                LocalDateTime.of(2023, 9, 15, 12, 0),
                LocalDateTime.of(2024, 9, 15, 12, 0));
    }

}
