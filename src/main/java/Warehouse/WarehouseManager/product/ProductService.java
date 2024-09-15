package Warehouse.WarehouseManager.product;

import Warehouse.WarehouseManager.enums.ProductSize;
import Warehouse.WarehouseManager.exception.ProductAlreadyExistsException;
import Warehouse.WarehouseManager.exception.ProductNotExistsException;
import Warehouse.WarehouseManager.exception.ProductQuantityException;
import Warehouse.WarehouseManager.exception.StockNotExistsException;
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

    @Autowired
    public ProductService(ProductRepository productRepository, StockRepository stockRepository) {
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
    }

    public List<ProductDto> getProductList(){
        return productRepository.findAll().stream().map(Product::toProductDto).toList();
    }

    public ProductDto getProductByProductName(String name){
        Product product = productRepository.findByName(name)
                .orElseThrow(() -> new ProductNotExistsException(name));
        return product.toProductDto();
    }

    public List<ProductDto> getProductListBySize(ProductSize size){
        List<Product> productList = productRepository.findBySize(size);
       return productList.stream().map(product -> product.toProductDto()).toList();
    }

    @Transactional
    public ProductDto addProductToDataBase(ProductDto productDto){
        if(productRepository.existsByName(productDto.name())){
            throw new ProductAlreadyExistsException(productDto.name());
        }
        Product product = new Product();
        product.setName(productDto.name());
        product.setSize(productDto.size());
        return productRepository.save(product).toProductDto();
    }

    @Transactional
    public ProductDto modifyProductInformation(String nameOfModifiedProduct,ProductDto productDto){
        Product product = productRepository
                .findByName(nameOfModifiedProduct).orElseThrow(()-> new ProductNotExistsException(nameOfModifiedProduct));
        product.setName(productDto.name());
        return productRepository.save(product).toProductDto();
    }


    @Transactional
    public void deleteProduct(String productName){
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
