package Warehouse.WarehouseManager;


import Warehouse.WarehouseManager.enums.ProductSize;
import Warehouse.WarehouseManager.exception.ProductAlreadyExistsException;
import Warehouse.WarehouseManager.exception.ProductNotExistsException;
import Warehouse.WarehouseManager.exception.ProductQuantityException;
import Warehouse.WarehouseManager.exception.StockNotExistsException;
import Warehouse.WarehouseManager.product.Product;
import Warehouse.WarehouseManager.product.ProductDto;
import Warehouse.WarehouseManager.product.ProductRepository;
import Warehouse.WarehouseManager.product.ProductService;
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
     //algorithm
    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    public void shouldReturnProductList(){
        //given
        when(productRepository.findAll()).thenReturn(createProductsForTest());
        //when
        List<ProductDto> productDtoList = productService.getDtoProductList();
        //then
        assertEquals(6,productDtoList.size());
    }

    @Test
    public void shouldFindProductByName(){
        //given
        Product product = new Product(1L,"Product1", ProductSize.MEDIUM);
        String name = "Product1";
        when(productRepository.findByName(name)).thenReturn(Optional.of(product));
        //when
        ProductDto productDto = productService.getProductDtoByProductName(name);
        //then
        assertNotNull(productDto);
        assertEquals(1L,productDto.id());
        assertEquals("Product1",productDto.name());
        assertEquals(ProductSize.MEDIUM,productDto.size());
    }

    @Test
    public void shouldThrowProductNotExistsExceptionWhenProductNotExists(){
        assertThrows(ProductNotExistsException.class,() -> productService.getProductDtoByProductName("Product1"));
    }

    @Test
    public void shouldFindProductListByProductSize(){
        //given
        List<Product> mediumSize = createProductsForTest().stream()
                .filter(product -> product.getSize().equals(ProductSize.MEDIUM)).toList();
        when(productRepository.findBySize(ProductSize.MEDIUM)).thenReturn(mediumSize);
        //when
        List<ProductDto> productDtoList = productService.getProductDtoListBySize(ProductSize.MEDIUM);
        //then
        assertEquals(4,productDtoList.size());
        assertEquals(ProductSize.MEDIUM,productDtoList.get(0).size());
        assertEquals(ProductSize.MEDIUM,productDtoList.get(1).size());
        assertEquals(ProductSize.MEDIUM,productDtoList.get(2).size());
        assertEquals(ProductSize.MEDIUM,productDtoList.get(3).size());
    }

    @Test
    public void shouldThrowProductAlreadyExistsExceptionWhenProductExists(){
        //given
        Product product = new Product(1L,"Product1", ProductSize.MEDIUM);
        Product product1 = new Product(2L,"Product1", ProductSize.MEDIUM);
        when(productRepository.existsByName(product.getName())).thenReturn(true);
        //when then
        assertThrows(ProductAlreadyExistsException.class, () -> productService.addProductToDataBase(product1.toProductDto()));

    }

    @Test
    public void shouldThrowIllegalArgumentsExceptionWhenProductSizeIsNull(){
        //given
        Product product = new Product();
        //when then
        assertThrows(IllegalArgumentException.class,() -> productService.addProductToDataBase(product.toProductDto()));
    }

    @Test
    public void shouldAddProductToDataBase(){
        //given
        Product product = new Product(1L,"Product1", ProductSize.MEDIUM);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        //when
        ProductDto productDto = productService.addProductToDataBase(product.toProductDto());
        //then
        assertNotNull(productDto);
        assertEquals("Product1",productDto.name());
        assertEquals(1L, productDto.id());
        assertEquals(ProductSize.MEDIUM, productDto.size());
    }

    @Test
    public void shouldModifyProductName(){
        //given
        Product product = new Product(1L,"Product1", ProductSize.MEDIUM);
        Product modifyProduct = new Product(1L,"Product2",ProductSize.MEDIUM);
        ProductDto productDto = new ProductDto(1L,"Product2",ProductSize.MEDIUM);
        when(productRepository.findByName(product.getName())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(modifyProduct);
        //when
        ProductDto productModified = productService.modifyProductInformation(product.getName(),productDto);
        //then
        assertNotNull(productModified);
        assertEquals("Product2",productModified.name());
        assertEquals(1L,productModified.id());
        assertEquals("Product2",productModified.name());
    }

    @Test
    public void shouldModifyThrowProductNotExistsExceptionWhenProductNotExists(){
        //given
        ProductDto productDto = new ProductDto(1L,"Product2",ProductSize.MEDIUM);
        //when then
        assertThrows(ProductNotExistsException.class,
                () -> productService.modifyProductInformation("Product1",productDto));
    }


    @Test
    public void shouldDeleteProduct(){
        //given
        Product product = new Product(1L,"Product2",ProductSize.MEDIUM);
        Stock stock = new Stock();
        stock.setQuantity(0L);
        when(productRepository.findByName(product.getName())).thenReturn(Optional.of(product));
        when(stockRepository.findStockByProductName(product.getName())).thenReturn(Optional.of(stock));
        //when
        productService.deleteProduct(product.getName());
        //then
        verify(stockRepository, times(1)).delete(stock);
        verify(productRepository, times(1)).delete(product);
    }

    @Test
    public void deleteProductShouldThrowProductNotExistsExceptionWhenProductNotFound(){
        Product product = new Product();
        assertThrows(ProductNotExistsException.class, () -> productService.deleteProduct(product.getName()));
    }

    @Test
    public void deleteProductShouldThrowProductQuantityExceptionWhenQuantityIsMoreThanZero(){
        //given
        Product product = new Product(1L,"Product2",ProductSize.MEDIUM);
        Stock stock = new Stock();
        stock.setQuantity(5L);
        when(productRepository.findByName(product.getName())).thenReturn(Optional.of(product));
        when(stockRepository.findStockByProductName(product.getName())).thenReturn(Optional.of(stock));
        //when
        assertThrows(ProductQuantityException.class, () -> productService.deleteProduct(product.getName()));
    }

    @Test
    public void deleteProductShouldThrowStockNotExistsExceptionWhenStockNotFound(){
        Product product = new Product(1L,"Product2",ProductSize.MEDIUM);
        when(productRepository.findByName(product.getName())).thenReturn(Optional.of(product));
        Stock stock = new Stock();
        stock.setQuantity(0L);
        when(stockRepository.findStockByProductName(product.getName())).thenReturn(Optional.empty());
        assertThrows(StockNotExistsException.class, () -> productService.deleteProduct(product.getName()));

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
