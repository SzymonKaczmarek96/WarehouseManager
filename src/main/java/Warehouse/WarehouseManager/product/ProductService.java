package Warehouse.WarehouseManager.product;

import Warehouse.WarehouseManager.employee.EmployeeDto;
import Warehouse.WarehouseManager.enums.ProductSize;
import Warehouse.WarehouseManager.enums.Resource;
import Warehouse.WarehouseManager.enums.WarehouseSystemOperation;
import Warehouse.WarehouseManager.exception.*;
import Warehouse.WarehouseManager.security.SecurityService;
import Warehouse.WarehouseManager.stock.Stock;
import Warehouse.WarehouseManager.stock.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private ProductRepository productRepository;

    private StockRepository stockRepository;

    private SecurityService securityService;

    @Autowired
    public ProductService(ProductRepository productRepository, StockRepository stockRepository
            ,SecurityService securityService) {
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
        this.securityService = securityService;
    }

    public List<ProductDto> getDtoProductList(String bearerToken){
        if(checkEmployeeAccess(bearerToken,WarehouseSystemOperation.STORE,Resource.PRODUCT)){
            throw new AccessDeniedException();
        }
        return productRepository.findAll().stream().map(Product::toProductDto).toList();
    }

    @Transactional
    public ProductDto getProductDtoByProductName(String name, String bearerToken){
        if(checkEmployeeAccess(bearerToken,WarehouseSystemOperation.STORE,Resource.PRODUCT)){
            throw new AccessDeniedException();
        }
        Product product = productRepository.findByName(name)
                .orElseThrow(() -> new ProductNotExistsException(name));
        return product.toProductDto();
    }

    @Transactional
    public List<ProductDto> getProductDtoListBySize(ProductSize size,String bearerToken){
        if(checkEmployeeAccess(bearerToken, WarehouseSystemOperation.STORE,Resource.PRODUCT)){
            throw new AccessDeniedException();
        }
        List<Product> productList = productRepository.findBySize(size);
       return productList.stream().map(product -> product.toProductDto()).toList();
    }

    @Transactional
    public ProductDto addProductDtoToDataBase(ProductDto productDto,String bearerToken){
        if(checkEmployeeAccess(bearerToken,WarehouseSystemOperation.ADD,Resource.PRODUCT)){
            throw new AccessDeniedException();
        }
        if(productRepository.existsByName(productDto.name())){
            throw new ProductAlreadyExistsException(productDto.name());
        }
        Product product = new Product();
        product.setName(productDto.name());
        product.setSize(productDto.size());
        return productRepository.save(product).toProductDto();
    }

    @Transactional
    public ProductDto modifyProductInformation(String nameOfModifiedProduct,ProductDto productDto,String bearerToken){
        if(checkEmployeeAccess(bearerToken,WarehouseSystemOperation.MODIFY,Resource.PRODUCT)){
            throw new AccessDeniedException();
        }
        Product product = productRepository
                .findByName(nameOfModifiedProduct).orElseThrow(()-> new ProductNotExistsException(nameOfModifiedProduct));
        product.setName(productDto.name());
        return productRepository.save(product).toProductDto();
    }


    @Transactional
    public void deleteProduct(String productName,String bearerToken){
        if(checkEmployeeAccess(bearerToken,WarehouseSystemOperation.REMOVAL,Resource.PRODUCT)){
            throw new AccessDeniedException();
        }
        Product product = productRepository
                .findByName(productName).orElseThrow(()-> new ProductNotExistsException(productName));
        if(checkProductQuantity(productName)){
                throw new ProductQuantityException();
            }
        Stock stock = stockRepository.findStockByProductName(productName).orElseThrow(StockNotExistsException::new);
        stockRepository.delete(stock);
        productRepository.delete(product);
    }


    private boolean checkProductQuantity(String productName){
        return stockRepository.findStockByProductName(productName)
                .map(stock -> stock.getQuantity() > 0)
                .orElse(false);
    }

    private boolean checkEmployeeAccess(String bearerToken,WarehouseSystemOperation warehouseSystemOperation, Resource resource){
        EmployeeDto employeeDto = securityService.getEmployeeDtoFromBearerToken(bearerToken);
        if(!employeeDto.role().hasAccessTo(warehouseSystemOperation, resource)){
            return true;
        }
        return false;
    }
}
