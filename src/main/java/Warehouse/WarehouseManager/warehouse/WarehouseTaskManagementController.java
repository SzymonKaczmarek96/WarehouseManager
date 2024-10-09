package Warehouse.WarehouseManager.warehouse;


import Warehouse.WarehouseManager.enums.ApprovalStatus;
import Warehouse.WarehouseManager.enums.CompletionStatus;
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

    @GetMapping("/{warehouseId}/status/completion")
    public ResponseEntity<WarehouseTasks> getWarehouseTasksListByCompletionStatus(@PathVariable long warehouseId
            , @RequestBody CompletionStatus completionStatus) {
        return ResponseEntity.ok(warehouseTaskManagementService.getWarehouseTaskListByCompletionStatus(warehouseId,completionStatus));
    }

    @PostMapping("/{warehouseId}/add")
    public ResponseEntity<WarehouseTask> createWarehouseTask(@PathVariable long warehouseId, @RequestBody WarehouseTask warehouseTask){
        return ResponseEntity.ok().body(warehouseTaskManagementService.createWarehouseTask(warehouseTask,warehouseId));
    }

    @PatchMapping("/{warehouseId}/approval")
    public ResponseEntity<WarehouseTask> changeApprovalStatus(@PathVariable long warehouseId,@RequestBody long taskId
            ,@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken){
        return ResponseEntity.ok(warehouseTaskManagementService.changeApproval(warehouseId,taskId,bearerToken));
    }

    @PatchMapping("/{warehouseId}/completion")
    public ResponseEntity<WarehouseTask> changeCompletionStatus(@PathVariable long warehouseId
            ,@RequestBody WarehouseTask warehouseTask){
        return ResponseEntity.ok(warehouseTaskManagementService.completeWarehouseTask(warehouseTask,warehouseId));
    }




}
