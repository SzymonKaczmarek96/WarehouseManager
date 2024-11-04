package Warehouse.WarehouseManager.product;

import Warehouse.WarehouseManager.consumer.ProductConsumer;
import Warehouse.WarehouseManager.employee.EmployeeService;
import Warehouse.WarehouseManager.enums.ProductSize;
import Warehouse.WarehouseManager.enums.Resource;
import Warehouse.WarehouseManager.enums.Role;
import Warehouse.WarehouseManager.enums.WarehouseSystemOperation;
import Warehouse.WarehouseManager.exception.ProductAlreadyExistsException;
import Warehouse.WarehouseManager.exception.ProductNotExistsException;
import Warehouse.WarehouseManager.exception.ProductQuantityException;
import Warehouse.WarehouseManager.exception.StockNotExistsException;
import Warehouse.WarehouseManager.reportgenerator.PDFReportGenerator;
import Warehouse.WarehouseManager.security.SecurityService;
import Warehouse.WarehouseManager.stock.Stock;
import Warehouse.WarehouseManager.stock.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private ProductRepository productRepository;

    private StockRepository stockRepository;

    private SecurityService securityService;

    private EmployeeService employeeService;

    private KafkaTemplate<Long,ProductDto> kafkaTemplate;

    private final String TOPIC_NAME = "new-topic";


    @Autowired
    public ProductService(ProductRepository productRepository, StockRepository stockRepository
            ,SecurityService securityService, EmployeeService employeeService,KafkaTemplate kafkaTemplate) {
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
        this.securityService = securityService;
        this.employeeService = employeeService;
        this.kafkaTemplate = kafkaTemplate;
    }

    public List<ProductDto> getDtoProductList(long employeeId){
        Role role = employeeService.getEmployeeRoleByEmployeeId(employeeId);
        securityService.checkEmployeeAccess(role,WarehouseSystemOperation.STORE,Resource.PRODUCT);
        return productRepository.findAll().stream().map(Product::toProductDto).toList();
    }

    @Transactional
    public ProductDto getProductDtoByProductName(String name, long employeeId){
        Role role = employeeService.getEmployeeRoleByEmployeeId(employeeId);
        securityService.checkEmployeeAccess(role,WarehouseSystemOperation.STORE,Resource.PRODUCT);
        Product product = productRepository.findByName(name)
                .orElseThrow(() -> new ProductNotExistsException(name));
        return product.toProductDto();
    }

    @Transactional
    public List<ProductDto> getProductDtoListBySize(ProductSize size,long employeeId){
        Role role = employeeService.getEmployeeRoleByEmployeeId(employeeId);
        securityService.checkEmployeeAccess(role,WarehouseSystemOperation.STORE,Resource.PRODUCT);
        List<Product> productList = productRepository.findBySize(size);
       return productList.stream().map(product -> product.toProductDto()).toList();
    }

    @Transactional
    public ProductDto addProductDto(ProductDto productDto, long employeeId){
        Role role = employeeService.getEmployeeRoleByEmployeeId(employeeId);
        securityService.checkEmployeeAccess(role,WarehouseSystemOperation.ADD,Resource.PRODUCT);
        if(productRepository.existsByName(productDto.name())){
            throw new ProductAlreadyExistsException(productDto.name());
        }
        Product product = new Product();
        product.setName(productDto.name());
        product.setSize(productDto.size());
        kafkaTemplate.send(TOPIC_NAME,product.toProductDto());
        return productRepository.save(product).toProductDto();
    }

    @Transactional
    public ProductDto modifyProductInformation(ProductDto productDto,long employeeId,String productName){
        Role role = employeeService.getEmployeeRoleByEmployeeId(employeeId);
        securityService.checkEmployeeAccess(role,WarehouseSystemOperation.MODIFY,Resource.PRODUCT);
        Product product = productRepository
                .findByName(productName).orElseThrow(()-> new ProductNotExistsException(productName));
        product.setName(productDto.name());
        return productRepository.save(product).toProductDto();
    }


    @Transactional
    public void deleteProduct(String productName,long employeeId){
        Role role = employeeService.getEmployeeRoleByEmployeeId(employeeId);
        securityService.checkEmployeeAccess(role,WarehouseSystemOperation.REMOVAL,Resource.PRODUCT);
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
}
