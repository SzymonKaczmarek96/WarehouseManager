package Warehouse.WarehouseManager;


import Warehouse.WarehouseManager.employee.Employee;
import Warehouse.WarehouseManager.employee.EmployeeDto;
import Warehouse.WarehouseManager.employee.EmployeeRepository;
import Warehouse.WarehouseManager.employee.EmployeeService;
import Warehouse.WarehouseManager.enums.ProductSize;
import Warehouse.WarehouseManager.enums.Resource;
import Warehouse.WarehouseManager.enums.Role;
import Warehouse.WarehouseManager.enums.WarehouseSystemOperation;
import Warehouse.WarehouseManager.exception.*;
import Warehouse.WarehouseManager.product.Product;
import Warehouse.WarehouseManager.product.ProductDto;
import Warehouse.WarehouseManager.product.ProductRepository;
import Warehouse.WarehouseManager.product.ProductService;
import Warehouse.WarehouseManager.security.SecurityService;
import Warehouse.WarehouseManager.stock.Stock;
import Warehouse.WarehouseManager.stock.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private SecurityService securityService;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private KafkaTemplate<Long,ProductDto> kafkaTemplate;

    @InjectMocks
    private ProductService productService;

    @Test
    public void shouldReturnProductDtoList(){
        //given
        when(employeeService.getEmployeeRoleByEmployeeId(1L)).thenReturn(createEmployeeBusinessOwner().getRole());
        when(productRepository.findAll()).thenReturn(createProductsForTest());
        //when
        List<ProductDto> productDtoList = productService.getDtoProductList(1L);
        //then
        assertEquals(6,productDtoList.size());
    }

    @Test
    public void shouldThrowAccessDeniedExceptionByReturnProductListWhenEmployeeHasNoAccess(){
        //given
        when(employeeService.getEmployeeRoleByEmployeeId(2L)).thenReturn(createEmployeeWarehouseOperator().getRole());
        doThrow(new AccessDeniedException()).when(securityService)
                .checkEmployeeAccess(createEmployeeWarehouseOperator().getRole(), WarehouseSystemOperation.STORE, Resource.PRODUCT);
        //when
        assertThrows(AccessDeniedException.class,()-> productService.getDtoProductList(2L));
    }

    @Test
    public void shouldThrowEmployeeNotExistsExceptionByReturnProductListWhenEmployeeNotExists(){
        when(employeeService.getEmployeeRoleByEmployeeId(3L)).thenThrow(new EmployeeNotExistsException("Employee"));
        assertThrows(EmployeeNotExistsException.class,() -> productService.getDtoProductList(3L));
    }

    @Test
    public void shouldReturnProductDtoWhenProductExistsAndEmployeeHasAccess(){
        //given
        when(employeeService.getEmployeeRoleByEmployeeId(1L)).thenReturn(createEmployeeBusinessOwner().getRole());
        when(productRepository.findByName("Product1")).thenReturn(Optional.of(createMediumProductForTest()));
        //when
        ProductDto productDto = productService.getProductDtoByProductName("Product1",1L);
        //then
        assertNotNull(productDto);
        assertEquals(1L,productDto.id());
        assertEquals("Product1",productDto.name());
        assertEquals(ProductSize.MEDIUM,productDto.size());
    }

    @Test
    public void shouldThrowAccessDeniedExceptionWhenEmployeeHasNoAccess() {
        //given
        when(employeeService.getEmployeeRoleByEmployeeId(2L)).thenReturn(createEmployeeWarehouseOperator().getRole());
        doThrow(new AccessDeniedException()).when(securityService)
                .checkEmployeeAccess(createEmployeeWarehouseOperator().getRole(), WarehouseSystemOperation.STORE, Resource.PRODUCT);
        //when
        assertThrows(AccessDeniedException.class, () -> productService.getProductDtoByProductName("Product1", 2L));
    }

    @Test
    public void shouldThrowProductNotExistsExceptionWhenProductNoExists() {
        //given
        when(employeeService.getEmployeeRoleByEmployeeId(1L)).thenReturn(createEmployeeBusinessOwner().getRole());
        //when
        assertThrows(ProductNotExistsException.class, () -> productService.getProductDtoByProductName("Product1", 1L));
    }

    @Test
    public void shouldThrowEmployeeNotExistsExceptionByGetProductListWhenEmployeeNotExists(){
        when(employeeService.getEmployeeRoleByEmployeeId(1L)).thenThrow(new EmployeeNotExistsException("Employee"));
        assertThrows(EmployeeNotExistsException.class,
                () -> productService.getProductDtoByProductName("Product1", 1L));;
    }

    @Test
    public void shouldThrowProductAlreadyExistsExceptionWhenProductExists(){
        //given
        when(employeeService.getEmployeeRoleByEmployeeId(1L)).thenReturn(createEmployeeBusinessOwner().getRole());
        Product product1 = new Product(2L,"Product1", ProductSize.MEDIUM);
        when(productRepository.existsByName("Product1")).thenReturn(true);
        //when then
        assertThrows(ProductAlreadyExistsException.class, () -> productService.addProductDto(product1.toProductDto(),1L));
    }

    @Test
    public void shouldThrowIllegalArgumentsExceptionWhenProductSizeIsNull(){
        //given
        when(employeeService.getEmployeeRoleByEmployeeId(1L)).thenReturn(createEmployeeBusinessOwner().getRole());
        //when then
        assertThrows(IllegalArgumentException.class,() -> productService.addProductDto(new Product().toProductDto(),1L));
    }
    @Test
    public void shouldFindProductListByProductSizeAndEmployeeHasAccess(){
        //given
        when(employeeService.getEmployeeRoleByEmployeeId(1L)).thenReturn(createEmployeeBusinessOwner().getRole());
        when(productRepository.findBySize(ProductSize.MEDIUM)).thenReturn(createProductsListWithMediumSize());
        //when
        List<ProductDto> productDtoList = productService.getProductDtoListBySize(ProductSize.MEDIUM,1L);
        //then
        assertEquals(4,productDtoList.size());
        assertEquals(ProductSize.MEDIUM,productDtoList.get(0).size());
        assertEquals(ProductSize.MEDIUM,productDtoList.get(1).size());
        assertEquals(ProductSize.MEDIUM,productDtoList.get(2).size());
        assertEquals(ProductSize.MEDIUM,productDtoList.get(3).size());
    }
    @Test
    public void shouldThrowAccessDeniedExceptionByFindProductListWhenEmployeeHasNoAccess(){
        //given
        when(employeeService.getEmployeeRoleByEmployeeId(1L)).thenReturn(createEmployeeWarehouseOperator().getRole());
        doThrow(new AccessDeniedException()).when(securityService)
                .checkEmployeeAccess(createEmployeeWarehouseOperator().getRole(), WarehouseSystemOperation.STORE, Resource.PRODUCT);
        //when
        assertThrows(AccessDeniedException.class,() -> productService.getProductDtoListBySize(ProductSize.MEDIUM,1L));

    }


    @Test
    public void shouldAddProductToDataBase(){
        //given
        when(employeeService.getEmployeeRoleByEmployeeId(1L)).thenReturn(createEmployeeBusinessOwner().getRole());
        when(productRepository.save(any(Product.class))).thenReturn(createMediumProductForTest());
        //when
        ProductDto productDto = productService.addProductDto(createMediumProductForTest().toProductDto(),1L);
        //then
        assertNotNull(productDto);
        assertEquals("Product1",productDto.name());
        assertEquals(1L, productDto.id());
        assertEquals(ProductSize.MEDIUM, productDto.size());
    }

    @Test
    public void shouldModifyProductNameWhenEmployeeHasAccess(){
        //given
        String productName = "Product1";
        when(employeeService.getEmployeeRoleByEmployeeId(1L)).thenReturn(createEmployeeBusinessOwner().getRole());
        when(productRepository.findByName(productName))
                .thenReturn(Optional.of(createMediumProductForTest()));
        when(productRepository.save(any(Product.class))).thenReturn(createProductWithModifiedInformation());
        //when
        ProductDto productModified = productService.modifyProductInformation(createProductWithModifiedInformation().toProductDto()
                ,1L,productName);
        //then
        assertNotNull(productModified);
        assertEquals("Product2",productModified.name());
        assertEquals(1L,productModified.id());
        assertEquals("Product2",productModified.name());
    }

    @Test
    public void shouldModifyThrowProductNotExistsExceptionWhenProductNotExists(){
        //given
        String productName = "Product1";
        when(employeeService.getEmployeeRoleByEmployeeId(1L)).thenReturn(createEmployeeBusinessOwner().getRole());
        //when then
        assertThrows(ProductNotExistsException.class,
                () -> productService.modifyProductInformation(createProductWithModifiedInformation().toProductDto()
                        ,1L,productName));
    }

    @Test
    public void shouldThrowAccessDeniedExceptionByModifyProductWhenEmployeeHasNoAccess(){
        //given
        String productName = "Product1";
        when(employeeService.getEmployeeRoleByEmployeeId(1L)).thenReturn(createEmployeeWarehouseOperator().getRole());
        doThrow(new AccessDeniedException()).when(securityService)
                .checkEmployeeAccess(createEmployeeWarehouseOperator().getRole(), WarehouseSystemOperation.MODIFY, Resource.PRODUCT);
        //when then
        assertThrows(AccessDeniedException.class,() -> productService.modifyProductInformation(createMediumProductForTest().toProductDto(),
                1L,productName));
    }

    @Test
    public void shouldThrowEmployeeNotExistsExceptionByModifyProductWhenEmployeeNotExists(){
        when(employeeService.getEmployeeRoleByEmployeeId(1L)).thenReturn(createEmployeeWarehouseOperator().getRole());
        doThrow(new AccessDeniedException()).when(securityService)
                .checkEmployeeAccess(createEmployeeWarehouseOperator().getRole(), WarehouseSystemOperation.MODIFY, Resource.PRODUCT);
        //when
        assertThrows(AccessDeniedException.class,() -> productService.modifyProductInformation(new Product().toProductDto(),1L,"Product1"));
    }

    @Test
    public void shouldDeleteProduct(){
        //given
        String productName = "Product1";
        Stock stock = createStockForTestWithZeroQuantity();
        Product product = createMediumProductForTest();
        when(employeeService.getEmployeeRoleByEmployeeId(1L)).thenReturn(createEmployeeBusinessOwner().getRole());
        when(productRepository.findByName(productName)).thenReturn(Optional.of(product));
        when(stockRepository.findStockByProductName(productName)).thenReturn(Optional.of(stock));
        //when
        productService.deleteProduct(productName,1L);
        //then
        verify(stockRepository, times(1)).delete(stock);
        verify(productRepository, times(1)).delete(product);
    }

    @Test
    public void deleteProductShouldThrowProductNotExistsExceptionWhenProductNotFound(){
        //given
        when(employeeService.getEmployeeRoleByEmployeeId(1L)).thenReturn(createEmployeeBusinessOwner().getRole());
        Product product = new Product();
        //when
        assertThrows(ProductNotExistsException.class, () -> productService.deleteProduct(product.getName(),1L));
    }

    @Test
    public void shouldThrowProductQuantityExceptionByDeleteProductWhenQuantityIsMoreThanZero(){
        //given
        String productName = "Product1";
        when(employeeService.getEmployeeRoleByEmployeeId(1L)).thenReturn(createEmployeeBusinessOwner().getRole());
        when(productRepository.findByName(productName)).thenReturn(Optional.of(createMediumProductForTest()));
        when(stockRepository.findStockByProductName(productName)).thenReturn(Optional.of(createStockForTestWithFiveQuantity()));
        //when
        assertThrows(ProductQuantityException.class, () -> productService.deleteProduct(productName,1L));
    }

    @Test
    public void shouldThrowStockNotExistsExceptionByDeleteProductWhenStockNotFound(){
        //given
        String productName = "Product1";
        when(employeeService.getEmployeeRoleByEmployeeId(1L)).thenReturn(createEmployeeBusinessOwner().getRole());
        when(productRepository.findByName(productName)).thenReturn(Optional.of(createMediumProductForTest()));
        when(stockRepository.findStockByProductName(productName)).thenReturn(Optional.empty());
        //when
        assertThrows(StockNotExistsException.class, () -> productService.deleteProduct(productName,1L));
    }

    @Test
    public void shouldThrowAccessDeniedExceptionByDeleteProductWhenEmployeeHasNoAccess(){
        //given
        String productName = "Product1";
        when(employeeService.getEmployeeRoleByEmployeeId(1L)).thenReturn(createEmployeeWarehouseOperator().getRole());
        doThrow(new AccessDeniedException()).when(securityService)
                .checkEmployeeAccess(createEmployeeWarehouseOperator().getRole(), WarehouseSystemOperation.REMOVAL, Resource.PRODUCT);
        //when
        assertThrows(AccessDeniedException.class,() -> productService.deleteProduct(productName,1L));
    }

    private List<Product> createProductsForTest(){
        Product product = new Product(1L,"Product1", ProductSize.MEDIUM);
        Product product1 = new Product(2L,"Product2", ProductSize.SMALL);
        Product product2 = new Product(3L,"Product3", ProductSize.SMALL);
        Product product3 = new Product(4L,"Product4", ProductSize.MEDIUM);
        Product product4 = new Product(5L,"Product5", ProductSize.MEDIUM);
        Product product5 = new Product(6L,"Product6", ProductSize.MEDIUM);
        return List.of(product,product1,product2,product3,product4,product5);
    }

    private List<Product> createProductsListWithMediumSize(){
        Product product1 = new Product(1L,"Product1", ProductSize.MEDIUM);
        Product product3 = new Product(4L,"Product4", ProductSize.MEDIUM);
        Product product4 = new Product(5L,"Product5", ProductSize.MEDIUM);
        Product product5 = new Product(6L,"Product6", ProductSize.MEDIUM);
        return List.of(product1,product3,product4,product5);
    }

    @Test
    public void shouldThrowEmployeeNotExistsExceptionByDeleteProductWhenEmployeeNotExists(){
        //given
        String productName = "Product1";
        when(employeeService.getEmployeeRoleByEmployeeId(1L)).thenReturn(createEmployeeWarehouseOperator().getRole());
        doThrow(new AccessDeniedException()).when(securityService)
                .checkEmployeeAccess(createEmployeeWarehouseOperator().getRole(), WarehouseSystemOperation.REMOVAL, Resource.PRODUCT);
        //when
        assertThrows(AccessDeniedException.class,() -> productService.deleteProduct(productName,1L));
    }

    public Employee createEmployeeBusinessOwner(){
        return new Employee(
                1L,
                "Szymon",
                "szymon@interia.pl",
                "hashed_password",
                true,
                Role.BUSINESS_OWNER,
                "access_token_value",
                "refresh_token_value",
                LocalDateTime.of(2023, 9, 15, 12, 0),
                LocalDateTime.of(2024, 9, 15, 12, 0));
    }

    public Employee createEmployeeWarehouseOperator(){
        return new Employee(
                2L,
                "Pawel",
                "pawel@interia.pl",
                "hashed_password",
                true,
                Role.WAREHOUSE_OPERATOR,
                "access_token_value",
                "refresh_token_value",
                LocalDateTime.of(2023, 9, 15, 12, 0),
                LocalDateTime.of(2024, 9, 15, 12, 0));
    }

    private Product createMediumProductForTest(){
        return new Product(1L,"Product1", ProductSize.MEDIUM);
    }

    private Product createProductWithModifiedInformation(){
        return new Product(1L,"Product2",ProductSize.MEDIUM);
    }

    private Stock createStockForTestWithZeroQuantity(){
        Stock stock = new Stock();
        stock.setQuantity(0L);
        return stock;
    }

    private Stock createStockForTestWithFiveQuantity(){
        Stock stock = new Stock();
        stock.setQuantity(5L);
        return stock;
    }



}
