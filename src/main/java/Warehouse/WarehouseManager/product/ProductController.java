package Warehouse.WarehouseManager.product;

import Warehouse.WarehouseManager.enums.ProductSize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
    public ResponseEntity<List<ProductDto>> getProductList(@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken){
        return ResponseEntity.ok(productService.getDtoProductList(bearerToken));
    }

    @GetMapping("/{name}")
    public ResponseEntity<ProductDto> getProductByProductName(@PathVariable String name
            ,@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken){
        return ResponseEntity.ok(productService.getProductDtoByProductName(name,bearerToken));
    }

    @GetMapping("/size")
    public ResponseEntity<List<ProductDto>> getProductListByProductSize(@RequestBody ProductSize size
            ,@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken){
        return ResponseEntity.ok().body(productService.getProductDtoListBySize(size,bearerToken));
    }

    @PostMapping("/add")
    public ResponseEntity<ProductDto> addProduct(@RequestBody ProductDto productDto
            ,@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken){
        return ResponseEntity.ok().body(productService.addProductDtoToDataBase(productDto,bearerToken));
    }

    @PutMapping("/{name}/update")
    public ResponseEntity<ProductDto> updateProductInformation(@PathVariable String name, @RequestBody ProductDto productDto
    ,@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerString){
        return ResponseEntity.ok(productService.modifyProductInformation(name,productDto,bearerString));
    }

    @DeleteMapping("/{name}/delete")
    public ResponseEntity deleteProduct(@PathVariable String name,@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken){
        productService.deleteProduct(name,bearerToken);
        return ResponseEntity.noContent().build();
    }





}
