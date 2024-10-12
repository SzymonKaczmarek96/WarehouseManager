package Warehouse.WarehouseManager;


import Warehouse.WarehouseManager.employee.Employee;
import Warehouse.WarehouseManager.employee.EmployeeDto;
import Warehouse.WarehouseManager.employee.EmployeeRepository;
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
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 40L, ApprovalStatus.NOT_APPROVED, LocalDate.now(),LocalDate.now(),
                Status.RECEPTION_AREA);
        Product product = new Product(1L,"Product",ProductSize.MEDIUM);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithTaskList()));
        when(productRepository.existsById(1L)).thenReturn(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        //when
        WarehouseTask task = warehouseManagementTaskService.createWarehouseTask(warehouseTask,1L);
        //then
        assertEquals(1L,warehouseTask.getId());
        assertEquals(1L,warehouseTask.getProductId());
        assertEquals(40,warehouseTask.getQuantity());
        assertTrue(task.getApprovalStatus().equals(ApprovalStatus.NOT_APPROVED));

        assertEquals(Status.RECEPTION_AREA,task.getStatus());
    }
    @Test
    public void shouldThrowWarehouseNotFoundExceptionWhenWarehouseNotExists(){
        //when then
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.NOT_APPROVED, LocalDate.now(),LocalDate.now(),
                Status.RECEPTION_AREA);
        assertThrows(WarehouseNotFoundException.class, () -> warehouseManagementTaskService.createWarehouseTask(
                warehouseTask,1L));
    }

    @Test
    public void shouldThrowProductNotExistsExceptionWhenProductNotExists(){
        //given
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.NOT_APPROVED, LocalDate.now(),LocalDate.now(),
                Status.RECEPTION_AREA);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithTaskList()));
        //when then
        assertThrows(ProductNotExistsException.class, () -> warehouseManagementTaskService.createWarehouseTask(
                warehouseTask,1L));
    }

    @Test
    public void shouldThrowStockQuantityExceptionWhenStockIsLessThenRelease(){
        //given
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 60L, ApprovalStatus.NOT_APPROVED, LocalDate.now(),LocalDate.now(),
                Status.RELEASE_AREA);
        Product product1 = new Product(1L,"Product", ProductSize.MEDIUM);
        Stock stock = new Stock(1L,product1,createWarehouseWithTaskList(),50L);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithTaskList()));
        when(productRepository.existsById(1L)).thenReturn(true);
        when(stockRepository.findStockByProductId(1L)).thenReturn(Optional.of(stock));
        //when then
        assertThrows(StockQuantityException.class,() -> warehouseManagementTaskService.createWarehouseTask(
                warehouseTask,1L));
    }

    @Test
    public void shouldThrowIncorrectStatusExceptionWhenStatusIsNotCorrect(){
        //given
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.NOT_APPROVED, LocalDate.now(),LocalDate.now(),
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
                1L, 1L, 50L, ApprovalStatus.NOT_APPROVED, LocalDate.now(),LocalDate.now(),
                Status.SHIPPED);
        Employee employee = new Employee(
                1L,
                "",
                "szymon@interia.pl",
                "hashed_password",
                true,
                Role.BUSINESS_OWNER,
                "access_token_value",
                "refresh_token_value",
                LocalDateTime.of(2023, 9, 15, 12, 0),
                LocalDateTime.of(2024, 9, 15, 12, 0));
        when(employeeService.getEmployeeRoleByEmployeeId(1L)).thenReturn(employee.getRole());
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithTaskList()));
        //when
        WarehouseTask task = warehouseManagementTaskService.changeApproval(1L,1L,1L);
        //then
        assertTrue(task.getApprovalStatus().equals(ApprovalStatus.APPROVED));
    }

    @Test
    public void shouldThrowAccessDeniedExceptionWhenEmployeeLacksModifyAccess(){
        //given
        Employee employee = new Employee(
                1L,
                "",
                "szymon@interia.pl",
                "hashed_password",
                true,
                Role.WAREHOUSE_OPERATOR,
                "access_token_value",
                "refresh_token_value",
                LocalDateTime.of(2023, 9, 15, 12, 0),
                LocalDateTime.of(2024, 9, 15, 12, 0));
        when(employeeService.getEmployeeRoleByEmployeeId(1L)).thenReturn(employee.getRole());
        doThrow(new AccessDeniedException()).when(securityService)
                .checkEmployeeAccess(employee.getRole(), WarehouseSystemOperation.MODIFY, Resource.WAREHOUSE_OPERATION);
        //when then
        assertThrows(AccessDeniedException.class, () ->
                warehouseManagementTaskService.changeApproval(1L,1L,1L));
    }

    @Test
    public void shouldThrowWarehouseNotFoundExceptionWhenChangingApprovalWithInvalidWarehouseId(){
        //given
        Employee employee = new Employee(
                1L,
                "",
                "szymon@interia.pl",
                "hashed_password",
                true,
                Role.BUSINESS_OWNER,
                "access_token_value",
                "refresh_token_value",
                LocalDateTime.of(2023, 9, 15, 12, 0),
                LocalDateTime.of(2024, 9, 15, 12, 0));
        when(employeeService.getEmployeeRoleByEmployeeId(1L)).thenReturn(employee.getRole());
        //when then
        assertThrows(WarehouseNotFoundException.class,()-> warehouseManagementTaskService.changeApproval(1L,1L,1L));
    }

    @Test
    public void shouldCompleteWarehouseOperationsWhenStatusIsReceptionArea(){
        //given
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.APPROVED, LocalDate.now(),LocalDate.now(),
                Status.RELEASE_AREA);
        Product product1 = new Product(1L,"Product", ProductSize.MEDIUM);
        Stock stock = new Stock(1L,product1,createWarehouseWithTaskList(),60L);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithTaskList()));
        when(productRepository.existsById(1L)).thenReturn(true);
        when(stockRepository.findStockByProductId(1L)).thenReturn(Optional.of(stock));
        //when
        WarehouseTask task = warehouseManagementTaskService.completeWarehouseTask(warehouseTask,1L);
        //given
        assertEquals(ApprovalStatus.DONE,task.getApprovalStatus());
    }

    @Test
    public void shouldCompleteWarehouseOperationsWhenStatusIsReleaseArea(){
        //given
        Product product1 = new Product(1L,"Product", ProductSize.MEDIUM);
        Stock stock = new Stock(1L,product1,createWarehouseWithTaskList(),60L);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithTaskList()));
        when(productRepository.existsById(1L)).thenReturn(true);
        when(stockRepository.findStockByProductId(1L)).thenReturn(Optional.of(stock));
        //when
        WarehouseTask task = warehouseManagementTaskService.completeWarehouseTask(createWarehouseTaskListForTest().get(0)
                ,1L);
        //then
        assertEquals(ApprovalStatus.APPROVED,task.getApprovalStatus());
    }

    @Test
    public void shouldThrowTaskNotApprovedExceptionWhenTaskIsNotApproved(){
        //given
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.NOT_APPROVED, LocalDate.now(),LocalDate.now(),
                Status.RELEASE_AREA);
        //when then
        assertThrows(TaskNotApprovedException.class,()-> warehouseManagementTaskService.completeWarehouseTask(warehouseTask,1L));
    }

    @Test
    public void shouldThrowIncorrectStatusExceptionWhenCompleteTaskHasNotCorrectStatus(){
        //given
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.APPROVED, LocalDate.now(),LocalDate.now(),
                Status.SHIPPED);;
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithTaskList()));
        //when then
        assertThrows(IncorrectStatusException.class,() -> warehouseManagementTaskService.completeWarehouseTask(warehouseTask,1L));
    }

    @Test
    public void shouldThrowWarehouseNotFoundExceptionWhenCompleteTaskWithInvalidWarehouseId() {
        //given
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.APPROVED, LocalDate.now(),LocalDate.now(),
                Status.RELEASE_AREA);
        //when then
        assertThrows(WarehouseNotFoundException.class,()-> warehouseManagementTaskService.completeWarehouseTask(warehouseTask,1L));
    }

    @Test
    public void shouldThrowProductNotExistsExceptionWhenCompleteTaskHasNotExistingProduct(){
        //given
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.APPROVED, LocalDate.now(),LocalDate.now(),
                Status.RECEPTION_AREA);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithTaskList()));
        //when then
        assertThrows(ProductNotExistsException.class, () -> warehouseManagementTaskService.createWarehouseTask(
                warehouseTask,1L));
    }


    @Test
    public void shouldThrowStockNotExistsExceptionWhenCompleteTaskHasNotExistingStock(){
        //given
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.APPROVED, LocalDate.now(),LocalDate.now(),
                Status.RELEASE_AREA);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithTaskList()));
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
        //given
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.APPROVED, LocalDate.now(),LocalDate.now(),
                Status.RELEASE_AREA);
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
    public void shouldDeleteTask(){
        //given
        WarehouseTask warehouseTask1 = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.APPROVED, LocalDate.now(),LocalDate.now(),
                Status.RECEPTION_AREA);
        Warehouse warehouse = createWarehouseWithTaskList();
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        //when
        warehouseManagementTaskService.deleteWarehouseTask(1L,warehouseTask1);
        //then
        assertEquals(2,warehouse.getWarehouseTasks().getWarehouseTaskList().size());
    }

    @Test
    public void shouldUpdateWarehouseTaskWhenStatusIsReceptionArea(){
        //given
        WarehouseTask warehouseTask1 = new WarehouseTask(
                3L, 4L, 100L, ApprovalStatus.NOT_APPROVED, LocalDate.now(),LocalDate.now(),
                Status.RECEPTION_AREA);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithTaskList()));
        when(productRepository.existsById(4L)).thenReturn(true);

        //when
        WarehouseTask warehouseTask = warehouseManagementTaskService.updateWarehouseTaskInformation(1L,warehouseTask1);
        //then
        assertEquals(3L,warehouseTask.getId());
        assertEquals(4L,warehouseTask.getProductId());
        assertEquals(100L,warehouseTask.getQuantity());
        assertEquals(ApprovalStatus.NOT_APPROVED,warehouseTask.getApprovalStatus());
        assertEquals(Status.RECEPTION_AREA,warehouseTask.getStatus());

    }

    @Test
    public void shouldUpdateWarehouseTaskWhenStatusIsReleaseArea(){
        //given
        WarehouseTask warehouseTask1 = new WarehouseTask(
                3L, 4L, 40L, ApprovalStatus.NOT_APPROVED, LocalDate.now(),LocalDate.now(),
                Status.RELEASE_AREA);
        Product product1 = new Product(4L,"Product", ProductSize.MEDIUM);
        Stock stock = new Stock(1L,product1,createWarehouseWithTaskList(),60L);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(createWarehouseWithTaskList()));
        when(productRepository.existsById(4L)).thenReturn(true);
        when(stockRepository.findStockByProductId(4L)).thenReturn(Optional.of(stock));
        //when
        WarehouseTask warehouseTask = warehouseManagementTaskService.updateWarehouseTaskInformation(1L,warehouseTask1);
        //then
        assertEquals(3L,warehouseTask.getId());
        assertEquals(4L,warehouseTask.getProductId());
        assertEquals(40L,warehouseTask.getQuantity());
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


}
