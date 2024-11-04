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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WarehouseServiceTest {
    @Mock
    private WarehouseRepository warehouseRepository;
    @InjectMocks
    private WarehouseService warehouseService;
    @Test
    public void shouldGetWarehouseList(){
        //given
        when(warehouseRepository.findAll()).thenReturn(createWarehousesForTest());
        //when
        List<WarehouseDto> warehouseDtoList = warehouseService.getWarehouseDtoList();
        //then
        assertNotNull(warehouseDtoList);
        assertEquals(3,warehouseDtoList.size());
    }

    @Test
    public void shouldFindWarehouseByWarehouseId(){
        //given
        WarehouseTask warehouseTask = new WarehouseTask(
                1L, 1L, 50L, ApprovalStatus.APPROVED, LocalDate.now(),LocalDate.now(),
                Status.RECEPTION_AREA);
        WarehouseTasks warehouseTasks = new WarehouseTasks();
        warehouseTasks.setWarehouseTaskList(new ArrayList<>(List.of(warehouseTask)));
        Warehouse warehouse = new Warehouse(1L,"M1",10000L,0L,warehouseTasks);
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        //when
        WarehouseDto warehouseDto = warehouseService.getWarehouseDtoById(1L);
        //then
        assertNotNull(warehouseDto);
        assertEquals("M1",warehouseDto.name());
        assertEquals(10000L,warehouseDto.capacity());
        assertEquals(0,warehouseDto.occupiedArea());
        assertEquals(1,warehouseDto.warehouseTasks().getWarehouseTaskList().size());

    }


    private List<Warehouse> createWarehousesForTest(){
        WarehouseTasks warehouseTasks = new WarehouseTasks();
        WarehouseTasks warehouseTasks1 = new WarehouseTasks();
        WarehouseTasks warehouseTasks2 = new WarehouseTasks();
        Warehouse warehouse = new Warehouse(1L,"M1",10000L,0L,warehouseTasks);
        Warehouse warehouse1 = new Warehouse(2L,"M2",10000L,0L,warehouseTasks1);
        Warehouse warehouse2 = new Warehouse(3L,"M1",10000L,0L,warehouseTasks2);
        return Arrays.asList(warehouse,warehouse1,warehouse2);
    }





}
