package Warehouse.WarehouseManager;


import Warehouse.WarehouseManager.employee.EmployeeDto;
import Warehouse.WarehouseManager.employee.EmployeeRepository;
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
    private Role role;

    @Mock
    private EmployeeDto employeeDto;

    @InjectMocks
    private ProductService productService;

    @Test
    public void shouldReturnProductDtoList(){
        //given
        when(employeeDto.role()).thenReturn(role);
        when(securityService.getEmployeeDtoFromBearerToken("token")).thenReturn(employeeDto);
        when(role.hasAccessTo(WarehouseSystemOperation.STORE,Resource.PRODUCT)).thenReturn(true);
        when(productRepository.findAll()).thenReturn(createProductsForTest());
        //when
        List<ProductDto> productDtoList = productService.getDtoProductList("token");
        //then
        assertEquals(6,productDtoList.size());
    }

    @Test
    public void shouldThrowAccessDeniedExceptionByReturnProductListWhenEmployeeHasNoAccess(){
        //given
        when(employeeDto.role()).thenReturn(role);
        when(securityService.getEmployeeDtoFromBearerToken("token")).thenReturn(employeeDto);
        when(role.hasAccessTo(WarehouseSystemOperation.STORE,Resource.PRODUCT)).thenReturn(false);
        //when
        assertThrows(AccessDeniedException.class,()-> productService.getDtoProductList("token"));
    }

    @Test
    public void shouldReturnProductDtoWhenProductExistsAndEmployeeHasAccess(){
        //given
        when(employeeDto.role()).thenReturn(role);
        when(securityService.getEmployeeDtoFromBearerToken("token")).thenReturn(employeeDto);
        when(role.hasAccessTo(WarehouseSystemOperation.STORE,Resource.PRODUCT)).thenReturn(true);
        Product product = new Product(1L,"Product1", ProductSize.MEDIUM);
        when(productRepository.findByName("Product1")).thenReturn(Optional.of(product));
        //when
        ProductDto productDto = productService.getProductDtoByProductName("Product1","token");
        //then
        assertNotNull(productDto);
        assertEquals(1L,productDto.id());
        assertEquals("Product1",productDto.name());
        assertEquals(ProductSize.MEDIUM,productDto.size());
    }

    @Test
    public void shouldThrowAccessDeniedExceptionWhenEmployeeHasNoAccess() {
        //given
        when(employeeDto.role()).thenReturn(role);
        when(role.hasAccessTo(WarehouseSystemOperation.STORE, Resource.PRODUCT)).thenReturn(false);
        when(securityService.getEmployeeDtoFromBearerToken("validToken")).thenReturn(employeeDto);
        //when
        assertThrows(AccessDeniedException.class, () -> productService.getProductDtoByProductName("Product1", "validToken"));
    }

    @Test
    public void shouldThrowProductNotExistsExceptionWhenProductNoExists() {
        //given
        when(employeeDto.role()).thenReturn(role);
        when(role.hasAccessTo(WarehouseSystemOperation.STORE, Resource.PRODUCT)).thenReturn(true);
        when(securityService.getEmployeeDtoFromBearerToken("validToken")).thenReturn(employeeDto);
        //when
        assertThrows(ProductNotExistsException.class, () -> productService.getProductDtoByProductName("Product1", "validToken"));
    }

    @Test
    public void shouldThrowProductAlreadyExistsExceptionWhenProductExists(){
        //given
        when(employeeDto.role()).thenReturn(role);
        when(role.hasAccessTo(WarehouseSystemOperation.ADD, Resource.PRODUCT)).thenReturn(true);
        when(securityService.getEmployeeDtoFromBearerToken("token")).thenReturn(employeeDto);
        Product product1 = new Product(2L,"Product1", ProductSize.MEDIUM);
        when(productRepository.existsByName("Product1")).thenReturn(true);
        //when then
        assertThrows(ProductAlreadyExistsException.class, () -> productService.addProductDtoToDataBase(product1.toProductDto(),"token"));
    }

    @Test
    public void shouldThrowIllegalArgumentsExceptionWhenProductSizeIsNull(){
        //given
        when(employeeDto.role()).thenReturn(role);
        when(role.hasAccessTo(WarehouseSystemOperation.ADD, Resource.PRODUCT)).thenReturn(true);
        when(securityService.getEmployeeDtoFromBearerToken("token")).thenReturn(employeeDto);
        Product product = new Product();
        //when then
        assertThrows(IllegalArgumentException.class,() -> productService.addProductDtoToDataBase(new Product().toProductDto(),"token"));
    }
    @Test
    public void shouldFindProductListByProductSizeAndEmployeeHasAccess(){
        //given
        when(employeeDto.role()).thenReturn(role);
        when(securityService.getEmployeeDtoFromBearerToken("token")).thenReturn(employeeDto);
        when(role.hasAccessTo(WarehouseSystemOperation.STORE,Resource.PRODUCT)).thenReturn(true);
        List<Product> mediumSize = createProductsForTest().stream()
                .filter(product -> product.getSize().equals(ProductSize.MEDIUM)).toList();
        when(productRepository.findBySize(ProductSize.MEDIUM)).thenReturn(mediumSize);
        //when
        List<ProductDto> productDtoList = productService.getProductDtoListBySize(ProductSize.MEDIUM,"token");
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
        when(employeeDto.role()).thenReturn(role);
        when(securityService.getEmployeeDtoFromBearerToken("token")).thenReturn(employeeDto);
        when(role.hasAccessTo(WarehouseSystemOperation.STORE,Resource.PRODUCT)).thenReturn(false);
        //when
        assertThrows(AccessDeniedException.class,() -> productService.getProductDtoListBySize(ProductSize.MEDIUM,"token"));

    }


    @Test
    public void shouldAddProductToDataBase(){
        //given
        when(employeeDto.role()).thenReturn(role);
        when(securityService.getEmployeeDtoFromBearerToken("token")).thenReturn(employeeDto);
        when(role.hasAccessTo(WarehouseSystemOperation.ADD,Resource.PRODUCT)).thenReturn(true);
        Product product = new Product(1L,"Product1", ProductSize.MEDIUM);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        //when
        ProductDto productDto = productService.addProductDtoToDataBase(product.toProductDto(),"token");
        //then
        assertNotNull(productDto);
        assertEquals("Product1",productDto.name());
        assertEquals(1L, productDto.id());
        assertEquals(ProductSize.MEDIUM, productDto.size());
    }

    @Test
    public void shouldModifyProductNameWhenEmployeeHasAccess(){
        //given
        when(employeeDto.role()).thenReturn(role);
        when(securityService.getEmployeeDtoFromBearerToken("token")).thenReturn(employeeDto);
        when(role.hasAccessTo(WarehouseSystemOperation.MODIFY,Resource.PRODUCT)).thenReturn(true);
        Product product = new Product(1L,"Product1", ProductSize.MEDIUM);
        Product modifyProduct = new Product(1L,"Product2",ProductSize.MEDIUM);
        ProductDto productDto = new ProductDto(1L,"Product2",ProductSize.MEDIUM);
        when(productRepository.findByName(product.getName())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(modifyProduct);
        //when
        ProductDto productModified = productService.modifyProductInformation(product.getName(),productDto,"token");
        //then
        assertNotNull(productModified);
        assertEquals("Product2",productModified.name());
        assertEquals(1L,productModified.id());
        assertEquals("Product2",productModified.name());
    }

    @Test
    public void shouldModifyThrowProductNotExistsExceptionWhenProductNotExists(){
        //given
        when(employeeDto.role()).thenReturn(role);
        when(securityService.getEmployeeDtoFromBearerToken("token")).thenReturn(employeeDto);
        when(role.hasAccessTo(WarehouseSystemOperation.MODIFY,Resource.PRODUCT)).thenReturn(true);
        ProductDto productDto = new ProductDto(1L,"Product2",ProductSize.MEDIUM);
        //when then
        assertThrows(ProductNotExistsException.class,
                () -> productService.modifyProductInformation("Product1",productDto,"token"));
    }


    @Test
    public void shouldThrowAccessDeniedExceptionByModifyProductWhenEmployeeHasNoAccess(){
        //given
        when(employeeDto.role()).thenReturn(role);
        when(securityService.getEmployeeDtoFromBearerToken("token")).thenReturn(employeeDto);
        when(role.hasAccessTo(WarehouseSystemOperation.MODIFY,Resource.PRODUCT)).thenReturn(false);
        //when then
        assertThrows(AccessDeniedException.class,() -> productService.modifyProductInformation("Product1",
                new Product().toProductDto(),"token"));
    }


    @Test
    public void shouldDeleteProduct(){
        //given
        when(employeeDto.role()).thenReturn(role);
        when(securityService.getEmployeeDtoFromBearerToken("token")).thenReturn(employeeDto);
        when(role.hasAccessTo(WarehouseSystemOperation.REMOVAL,Resource.PRODUCT)).thenReturn(true);
        Product product = new Product(1L,"Product2",ProductSize.MEDIUM);
        Stock stock = new Stock();
        stock.setQuantity(0L);
        when(productRepository.findByName(product.getName())).thenReturn(Optional.of(product));
        when(stockRepository.findStockByProductName(product.getName())).thenReturn(Optional.of(stock));
        //when
        productService.deleteProduct(product.getName(),"token");
        //then
        verify(stockRepository, times(1)).delete(stock);
        verify(productRepository, times(1)).delete(product);
    }

    @Test
    public void deleteProductShouldThrowProductNotExistsExceptionWhenProductNotFound(){
        //given
        when(employeeDto.role()).thenReturn(role);
        when(securityService.getEmployeeDtoFromBearerToken("token")).thenReturn(employeeDto);
        when(role.hasAccessTo(WarehouseSystemOperation.REMOVAL,Resource.PRODUCT)).thenReturn(true);
        Product product = new Product();
        //when
        assertThrows(ProductNotExistsException.class, () -> productService.deleteProduct(product.getName(),"token"));
    }

    @Test
    public void deleteProductShouldThrowProductQuantityExceptionWhenQuantityIsMoreThanZero(){
        //given
        when(employeeDto.role()).thenReturn(role);
        when(securityService.getEmployeeDtoFromBearerToken("token")).thenReturn(employeeDto);
        when(role.hasAccessTo(WarehouseSystemOperation.REMOVAL,Resource.PRODUCT)).thenReturn(true);
        Product product = new Product(1L,"Product2",ProductSize.MEDIUM);
        Stock stock = new Stock();
        stock.setQuantity(5L);
        when(productRepository.findByName(product.getName())).thenReturn(Optional.of(product));
        when(stockRepository.findStockByProductName(product.getName())).thenReturn(Optional.of(stock));
        //when
        assertThrows(ProductQuantityException.class, () -> productService.deleteProduct(product.getName(),"token"));
    }

    @Test
    public void deleteProductShouldThrowStockNotExistsExceptionWhenStockNotFound(){
        //given
        when(employeeDto.role()).thenReturn(role);
        when(securityService.getEmployeeDtoFromBearerToken("token")).thenReturn(employeeDto);
        when(role.hasAccessTo(WarehouseSystemOperation.REMOVAL,Resource.PRODUCT)).thenReturn(true);
        Product product = new Product(1L,"Product2",ProductSize.MEDIUM);
        when(productRepository.findByName(product.getName())).thenReturn(Optional.of(product));
        Stock stock = new Stock();
        stock.setQuantity(0L);
        when(stockRepository.findStockByProductName(product.getName())).thenReturn(Optional.empty());
        //when
        assertThrows(StockNotExistsException.class, () -> productService.deleteProduct(product.getName(),"token"));
    }

    @Test
    public void shouldThrowAccessDeniedExceptionIntoDeleteProductWhenEmployeeHasNoAccess(){
        //given
        when(employeeDto.role()).thenReturn(role);
        when(securityService.getEmployeeDtoFromBearerToken("token")).thenReturn(employeeDto);
        when(role.hasAccessTo(WarehouseSystemOperation.REMOVAL,Resource.PRODUCT)).thenReturn(false);
        //when
        assertThrows(AccessDeniedException.class,() -> productService.deleteProduct("Product","token"));
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

}
