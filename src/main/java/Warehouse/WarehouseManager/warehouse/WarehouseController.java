package Warehouse.WarehouseManager.warehouse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouse")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(final WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<WarehouseDto>> getWarehouseDtoList() {
        return ResponseEntity.ok(warehouseService.getWarehouseDtoList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarehouseDto> getWarehouseDtoById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(warehouseService.getWarehouseDtoById(id));
    }

    @PostMapping("/add")
    public ResponseEntity<WarehouseDto> addWarehouse(@RequestBody WarehouseDto warehouseDto, @RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken) {
        return ResponseEntity.ok(warehouseService.addWarehouse(warehouseDto, bearerToken));
    }

    @PatchMapping("/modify")
    public ResponseEntity<WarehouseDto> modifyWarehouse(@RequestBody WarehouseDto warehouseDto, @RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken) {
        return ResponseEntity.ok(warehouseService.modifyWarehouse(warehouseDto, bearerToken));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity deleteWarehouse(@PathVariable("id") Long id, @RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken) {
        warehouseService.deleteWarehouse(id, bearerToken);
        return ResponseEntity.noContent().build();
    }


}
