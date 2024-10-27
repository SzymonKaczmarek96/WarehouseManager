package Warehouse.WarehouseManager.product;

import Warehouse.WarehouseManager.enums.ProductSize;
import Warehouse.WarehouseManager.reportgenerator.PDFReportGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private ProductService productService;

    @Autowired
    public ProductController(ProductService productService,PDFReportGenerator pdfReportGenerator) {
        this.productService = productService;
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<List<ProductDto>> getProductList(@PathVariable long employeeId){
        return ResponseEntity.ok(productService.getDtoProductList(employeeId));
    }

    @GetMapping("/{productName}/{employeeId}")
    public ResponseEntity<ProductDto> getProductByProductName(@PathVariable String productName
            ,@PathVariable long employeeId){
        return ResponseEntity.ok(productService.getProductDtoByProductName(productName,employeeId));
    }

    @GetMapping("/{employeeId}/size")
    public ResponseEntity<List<ProductDto>> getProductListByProductSize(@RequestBody ProductSize size
            ,@PathVariable long employeeId){
        return ResponseEntity.ok().body(productService.getProductDtoListBySize(size,employeeId));
    }

    @PostMapping("/{employeeId}/add")
    public ResponseEntity<ProductDto> addProduct(@RequestBody ProductDto productDto
            ,@PathVariable long employeeId){
        return ResponseEntity.ok().body(productService.addProductDto(productDto,employeeId));
    }

    @PutMapping("/{productName}/{employeeId}/update")
    public ResponseEntity<ProductDto> updateProductInformation(@RequestBody ProductDto productDto
            ,@PathVariable String productName,@PathVariable long employeeId){
        return ResponseEntity.ok(productService.modifyProductInformation(productDto,employeeId,productName));
    }

    @DeleteMapping("/{productName}/{employeeId}")
    public ResponseEntity deleteProduct(@PathVariable String productName, @PathVariable long employeeId){
        productService.deleteProduct(productName,employeeId);
        return ResponseEntity.noContent().build();
    }

}
