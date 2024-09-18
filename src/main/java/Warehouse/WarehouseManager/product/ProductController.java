package Warehouse.WarehouseManager.product;

import Warehouse.WarehouseManager.enums.ProductSize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }


    @GetMapping
    public ResponseEntity<List<ProductDto>> getProductList(){
        return ResponseEntity.ok(productService.getDtoProductList());
    }

    @GetMapping("/{name}")
    public ResponseEntity<ProductDto> getProductByProductName(@PathVariable String name){
        return ResponseEntity.ok(productService.getProductDtoByProductName(name));
    }

    @GetMapping("/size")
    public ResponseEntity<List<ProductDto>> getProductListByProductSize(@RequestBody ProductSize size){
        return ResponseEntity.ok().body(productService.getProductDtoListBySize(size));
    }

    @PostMapping("/add")
    public ResponseEntity<ProductDto> addProduct(@RequestBody ProductDto productDto){
        return ResponseEntity.ok().body(productService.addProductToDataBase(productDto));
    }

    @PutMapping("/{name}/update")
    public ResponseEntity<ProductDto> updateProductInformation(@PathVariable String name, @RequestBody ProductDto productDto){
        return ResponseEntity.ok(productService.modifyProductInformation(name,productDto));
    }

    @DeleteMapping("/{name}/delete")
    public ResponseEntity deleteProduct(@PathVariable String name){
        productService.deleteProduct(name);
        return ResponseEntity.noContent().build();
    }





}
