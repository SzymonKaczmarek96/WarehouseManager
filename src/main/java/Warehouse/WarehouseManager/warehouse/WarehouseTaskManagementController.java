package Warehouse.WarehouseManager.warehouse;


import Warehouse.WarehouseManager.enums.ApprovalStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/task")
public class WarehouseTaskManagementController {

    private WarehouseTaskManagementService warehouseTaskManagementService;

    @Autowired
    public WarehouseTaskManagementController(WarehouseTaskManagementService warehouseTaskManagementService) {
        this.warehouseTaskManagementService = warehouseTaskManagementService;
    }

    @GetMapping("/{warehouseId}")
    public ResponseEntity<WarehouseTasks> getWarehouseTasks(@PathVariable long warehouseId){
        return ResponseEntity.ok(warehouseTaskManagementService.getWarehouseTasksList(warehouseId));
    }

    @GetMapping("/{warehouseId}/status/approved")
    public ResponseEntity<WarehouseTasks> getWarehouseTasksListByApprovedStatus(@PathVariable long warehouseId
            , @RequestBody ApprovalStatus approvalStatus) {
        return ResponseEntity.ok(warehouseTaskManagementService.getWarehouseTaskListByApproved(warehouseId,approvalStatus));
    }

    @PostMapping("/{warehouseId}/add")
    public ResponseEntity<WarehouseTask> createWarehouseTask(@PathVariable long warehouseId, @RequestBody WarehouseTask warehouseTask){
        return ResponseEntity.ok().body(warehouseTaskManagementService.createWarehouseTask(warehouseTask,warehouseId));
    }

    @PatchMapping("/{warehouseId}/{userId}/approval")
    public ResponseEntity<WarehouseTask> changeApprovalStatus(@PathVariable long warehouseId,@PathVariable long userId
            ,@RequestBody long taskId
            ){
        return ResponseEntity.ok(warehouseTaskManagementService.changeApproval(warehouseId,userId,taskId));
    }


    @DeleteMapping("/{warehouseId}")
    public ResponseEntity deleteTask(@PathVariable long warehouseId, @RequestBody WarehouseTask warehouseTask){
        warehouseTaskManagementService.deleteWarehouseTask(warehouseId,warehouseTask);
        return ResponseEntity.noContent().build();
    }




}
