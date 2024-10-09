package Warehouse.WarehouseManager;


import Warehouse.WarehouseManager.employee.EmployeeDto;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WarehouseManagementServiceTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private StockRepository stockRepository;
    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private SecurityService securityService;
    @Mock
    private EmployeeDto employeeDto;
    @Mock
    private Role role;

    @InjectMocks
    private WarehouseTaskManagementService warehouseManagementTaskService;


    @Test
    public void shouldGetWarehouseTaskList(){
        //given
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.NOT_APPROVED, LocalDate.now(), CompletionStatus.DONE,
                Status.RECEPTION_AREA);
        WarehouseTasks warehouseTasks = new WarehouseTasks();
        warehouseTasks.setWarehouseTaskList(new ArrayList<>(List.of(warehouseTask)));
        Warehouse warehouse = new Warehouse(1L,"M1",10000L,0L,warehouseTasks);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        //when
        WarehouseTasks warehouseTasksList = warehouseManagementTaskService.getWarehouseTasksList(1L);
        assertNotNull(warehouseTasksList);
        assertEquals(1,warehouseTasksList.getWarehouseTaskList().size());
    }
    @Test
    public void shouldCreateWarehouseTask(){
        //given
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 40L, ApprovalStatus.NOT_APPROVED, LocalDate.now(), CompletionStatus.NOT_DONE,
                Status.RECEPTION_AREA);
        Warehouse warehouse1 = new Warehouse(1L,"M1",1000000L,0L
                ,new WarehouseTasks(new ArrayList<>()));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse1));
        when(productRepository.existsById(1L)).thenReturn(true);
        //when
        WarehouseTask task = warehouseManagementTaskService.createWarehouseTask(warehouseTask,1L);
        //then
        assertEquals(1L,warehouseTask.getId());
        assertEquals(1L,warehouseTask.getProductId());
        assertEquals(40,warehouseTask.getQuantity());
        assertTrue(task.getApprovalStatus().equals(ApprovalStatus.NOT_APPROVED));
        assertTrue(task.getCompletionStatus().equals(CompletionStatus.NOT_DONE));
        assertEquals(Status.RECEPTION_AREA,task.getStatus());
    }
    @Test
    public void shouldThrowWarehouseNotFoundExceptionWhenWarehouseNotExists(){
        //when then
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.NOT_APPROVED, LocalDate.now(), CompletionStatus.NOT_DONE,
                Status.RECEPTION_AREA);
        assertThrows(WarehouseNotFoundException.class, () -> warehouseManagementTaskService.createWarehouseTask(
                warehouseTask,1L));
    }

    @Test
    public void shouldThrowProductNotExistsExceptionWhenProductNotExists(){
        //given
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.NOT_APPROVED, LocalDate.now(), CompletionStatus.NOT_DONE,
                Status.RECEPTION_AREA);
        Warehouse warehouse = new Warehouse(1L,"M1",1000000L,0L
                ,new WarehouseTasks(List.of(warehouseTask)));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        //when then
        assertThrows(ProductNotExistsException.class, () -> warehouseManagementTaskService.createWarehouseTask(
                warehouseTask,1L));
    }

    @Test
    public void shouldThrowStockQuantityExceptionWhenStockIsLessThenRelease(){
        //given
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 60L, ApprovalStatus.NOT_APPROVED, LocalDate.now(), CompletionStatus.NOT_DONE,
                Status.RELEASE_AREA);
        Warehouse warehouse = new Warehouse(1L,"M1",1000000L,0L
                ,new WarehouseTasks(List.of(warehouseTask)));
        Product product1 = new Product(1L,"Product", ProductSize.MEDIUM);
        Stock stock = new Stock(1L,product1,warehouse,50L);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(productRepository.existsById(1L)).thenReturn(true);
        when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));
        //when then
        assertThrows(StockQuantityException.class,() -> warehouseManagementTaskService.createWarehouseTask(
                warehouseTask,1L));
    }

    @Test
    public void shouldThrowIncorrectStatusExceptionWhenStatusIsNotCorrect(){
        //given
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.NOT_APPROVED, LocalDate.now(), CompletionStatus.NOT_DONE,
                Status.SHIPPED);
        Warehouse warehouse1 = new Warehouse(1L,"M1",1000000L,0L
                ,new WarehouseTasks(List.of(warehouseTask)));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse1));
        when(productRepository.existsById(1L)).thenReturn(true);
        //when then
        assertThrows(IncorrectStatusException.class,() -> warehouseManagementTaskService.createWarehouseTask(warehouseTask,1L));
    }

    @Test
    public void shouldChangeApproval(){
        //given
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.NOT_APPROVED, LocalDate.now(), CompletionStatus.NOT_DONE,
                Status.SHIPPED);
        Warehouse warehouse = new Warehouse(1L,"M1",1000000L,0L
                ,new WarehouseTasks(List.of(warehouseTask)));
        when(employeeDto.role()).thenReturn(role);
        when(securityService.getEmployeeDtoFromBearerToken("token")).thenReturn(employeeDto);
        when(role.hasAccessTo(WarehouseSystemOperation.MODIFY, Resource.WAREHOUSE_OPERATION)).thenReturn(true);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        //when
        WarehouseTask task = warehouseManagementTaskService.changeApproval(1L,1L,"token");
        //then
        assertTrue(task.getApprovalStatus().equals(ApprovalStatus.APPROVED));
    }

    @Test
    public void shouldThrowAccessDeniedExceptionWhenEmployeeLacksModifyAccess(){
        //given
        when(employeeDto.role()).thenReturn(role);
        when(securityService.getEmployeeDtoFromBearerToken("token")).thenReturn(employeeDto);
        when(role.hasAccessTo(WarehouseSystemOperation.MODIFY, Resource.WAREHOUSE_OPERATION)).thenReturn(false);
        //when then
        assertThrows(AccessDeniedException.class, () ->
                warehouseManagementTaskService.changeApproval(1L,1L,"token"));
    }

    @Test
    public void shouldThrowWarehouseNotFoundExceptionWhenChangingApprovalWithInvalidWarehouseId(){
        //given
        when(employeeDto.role()).thenReturn(role);
        when(securityService.getEmployeeDtoFromBearerToken("token")).thenReturn(employeeDto);
        when(role.hasAccessTo(WarehouseSystemOperation.MODIFY, Resource.WAREHOUSE_OPERATION)).thenReturn(true);
        //when then
        assertThrows(WarehouseNotFoundException.class,()-> warehouseManagementTaskService.changeApproval(1L,1L,"token"));
    }

    @Test
    public void shouldCompleteWarehouseOperationsWhenStatusIsReceptionArea(){
        //given
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.APPROVED, LocalDate.now(), CompletionStatus.NOT_DONE,
                Status.RELEASE_AREA);
        Warehouse warehouse = new Warehouse(1L,"M1",1000000L,0L
                ,new WarehouseTasks(List.of(warehouseTask)));
        Product product1 = new Product(1L,"Product", ProductSize.MEDIUM);
        Stock stock = new Stock(1L,product1,warehouse,60L);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(productRepository.existsById(1L)).thenReturn(true);
        when(stockRepository.findStockByProductId(1L)).thenReturn(Optional.of(stock));
        //when
        WarehouseTask task = warehouseManagementTaskService.completeWarehouseTask(warehouseTask,1L);
        //given
        assertTrue(warehouseTask.getCompletionStatus().equals(CompletionStatus.DONE));
    }

    @Test
    public void shouldCompleteWarehouseOperationsWhenStatusIsReleaseArea(){
        //given
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.APPROVED, LocalDate.now(), CompletionStatus.NOT_DONE,
                Status.RELEASE_AREA);
        Warehouse warehouse = new Warehouse(1L,"M1",1000000L,0L
                ,new WarehouseTasks(List.of(warehouseTask)));
        Product product1 = new Product(1L,"Product", ProductSize.MEDIUM);
        Stock stock = new Stock(1L,product1,warehouse,60L);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(productRepository.existsById(1L)).thenReturn(true);
        when(stockRepository.findStockByProductId(1L)).thenReturn(Optional.of(stock));
        //when
        WarehouseTask task = warehouseManagementTaskService.completeWarehouseTask(warehouseTask,1L);
        //then
        assertTrue(task.getCompletionStatus().equals(CompletionStatus.DONE));
    }

    @Test
    public void shouldThrowTaskNotApprovedExceptionWhenTaskIsNotApproved(){
        //given
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.NOT_APPROVED, LocalDate.now(), CompletionStatus.NOT_DONE,
                Status.RELEASE_AREA);
        //when then
        assertThrows(TaskNotApprovedException.class,()-> warehouseManagementTaskService.completeWarehouseTask(warehouseTask,1L));
    }

    @Test
    public void shouldThrowIncorrectStatusExceptionWhenCompleteTaskHasNotCorrectStatus(){
        //given
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.APPROVED, LocalDate.now(), CompletionStatus.NOT_DONE,
                Status.SHIPPED);
        Warehouse warehouse1 = new Warehouse(1L,"M1",1000000L,0L
                ,new WarehouseTasks(List.of(warehouseTask)));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse1));
        //when then
        assertThrows(IncorrectStatusException.class,() -> warehouseManagementTaskService.completeWarehouseTask(warehouseTask,1L));
    }

    @Test
    public void shouldThrowWarehouseNotFoundExceptionWhenCompleteTaskWithInvalidWarehouseId() {
        //given
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.APPROVED, LocalDate.now(), CompletionStatus.NOT_DONE,
                Status.RELEASE_AREA);
        //when then
        assertThrows(WarehouseNotFoundException.class,()-> warehouseManagementTaskService.completeWarehouseTask(warehouseTask,1L));
    }

    @Test
    public void shouldThrowProductNotExistsExceptionWhenCompleteTaskHasNotExistingProduct(){
        //given
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.APPROVED, LocalDate.now(), CompletionStatus.NOT_DONE,
                Status.RECEPTION_AREA);
        Warehouse warehouse = new Warehouse(1L,"M1",1000000L,0L
                ,new WarehouseTasks(List.of(warehouseTask)));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        //when then
        assertThrows(ProductNotExistsException.class, () -> warehouseManagementTaskService.createWarehouseTask(
                warehouseTask,1L));
    }


    @Test
    public void shouldThrowStockNotExistsExceptionWhenCompleteTaskHasNotExistingStock(){
        //given
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.APPROVED, LocalDate.now(), CompletionStatus.NOT_DONE,
                Status.RELEASE_AREA);
        Warehouse warehouse = new Warehouse(1L,"M1",1000000L,0L
                ,new WarehouseTasks(List.of(warehouseTask)));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(productRepository.existsById(1L)).thenReturn(true);
        //when then
        assertThrows(StockNotExistsException.class, () -> warehouseManagementTaskService.createWarehouseTask(
                warehouseTask,1L));
    }

    @Test
    public void shouldFindTaskListByApprovedStatus(){
        //given
        Warehouse warehouse = new Warehouse(1L,"M1",1000000L,0L
                ,new WarehouseTasks(createWarehouseTaskListForTest()));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        //when
        WarehouseTasks warehouseTaskList = warehouseManagementTaskService.getWarehouseTaskListByApproved(1L,ApprovalStatus.APPROVED);
        //then
        assertNotNull(warehouseTaskList);
        assertEquals(2,warehouseTaskList.getWarehouseTaskList().size());
    }

    @Test
    public void shouldFindTaskListByNotApprovedStatus(){
        //given
        Warehouse warehouse = new Warehouse(1L,"M1",1000000L,0L
                ,new WarehouseTasks(createWarehouseTaskListForTest()));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        //when
        WarehouseTasks warehouseTaskList = warehouseManagementTaskService.getWarehouseTaskListByApproved(1L,ApprovalStatus.NOT_APPROVED);
        //then
        assertNotNull(warehouseTaskList);
        assertEquals(1,warehouseTaskList.getWarehouseTaskList().size());
    }

    @Test
    public void shouldThrowWarehouseNotFoundExceptionWhenFindTaskByApprovedStatusWithInvalidWarehouseId(){
        //given
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.APPROVED, LocalDate.now(), CompletionStatus.NOT_DONE,
                Status.RELEASE_AREA);
        //when then
        assertThrows(WarehouseNotFoundException.class, () -> warehouseManagementTaskService.getWarehouseTaskListByApproved(1L,ApprovalStatus.APPROVED));
    }

    @Test
    public void shouldFindTaskListByCompletionStatusIsDone(){
        //given
        Warehouse warehouse = new Warehouse(1L,"M1",1000000L,0L
                ,new WarehouseTasks(createWarehouseTaskListForTest()));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        //when
        WarehouseTasks warehouseTaskList = warehouseManagementTaskService.getWarehouseTaskListByCompletionStatus(1L,CompletionStatus.DONE);
        //then
        assertNotNull(warehouseTaskList);
        assertEquals(2,warehouseTaskList.getWarehouseTaskList().size());
    }

    @Test
    public void shouldFindTaskListByCompletionStatusIsNotDone(){
        //given
        Warehouse warehouse = new Warehouse(1L,"M1",1000000L,0L
                ,new WarehouseTasks(createWarehouseTaskListForTest()));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        //when
        WarehouseTasks warehouseTaskList = warehouseManagementTaskService.getWarehouseTaskListByCompletionStatus(1L,CompletionStatus.NOT_DONE);
        //then
        assertNotNull(warehouseTaskList);
        assertEquals(1,warehouseTaskList.getWarehouseTaskList().size());
    }

    @Test
    public void shouldThrowWarehouseNotFoundExceptionWhenFindTaskByCompletionStatusWithInvalidWarehouseId(){
        //given
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.APPROVED, LocalDate.now(), CompletionStatus.NOT_DONE,
                Status.RELEASE_AREA);
        //when then
        assertThrows(WarehouseNotFoundException.class, () -> warehouseManagementTaskService.getWarehouseTaskListByCompletionStatus(1L,CompletionStatus.DONE));
    }




    private List<WarehouseTask> createWarehouseTaskListForTest(){
        WarehouseTask warehouseTask1 = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.APPROVED, LocalDate.now(), CompletionStatus.DONE,
                Status.RECEPTION_AREA);
        WarehouseTask warehouseTask2 = new WarehouseTask(
                2L, 1L, 50L, ApprovalStatus.APPROVED, LocalDate.now(), CompletionStatus.DONE,
                Status.RECEPTION_AREA);
        WarehouseTask warehouseTask3 = new WarehouseTask(
                2L, 1L, 50L, ApprovalStatus.NOT_APPROVED, LocalDate.now(), CompletionStatus.NOT_DONE,
                Status.RECEPTION_AREA);
        return Arrays.asList(warehouseTask1,warehouseTask2,warehouseTask3);
    }


}
