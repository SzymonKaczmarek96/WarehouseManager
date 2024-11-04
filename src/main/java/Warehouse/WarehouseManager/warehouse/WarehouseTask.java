package Warehouse.WarehouseManager.warehouse;

import Warehouse.WarehouseManager.enums.ApprovalStatus;
import Warehouse.WarehouseManager.enums.Status;
import Warehouse.WarehouseManager.exception.IncorrectStatusException;
import Warehouse.WarehouseManager.product.Product;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class WarehouseTask implements Serializable {
    @JsonProperty("task_id")
    private long id;

    @JsonProperty("product_id")
    private long productId;

    @JsonProperty("quantity")
    private long quantity;

    @Enumerated(EnumType.STRING)
    @JsonProperty("approval_status")
    private ApprovalStatus approvalStatus = ApprovalStatus.NOT_APPROVED;

    @JsonProperty("task_created_at")
    private LocalDate taskCreatedAt;

    @JsonProperty("task_updated_at")
    private LocalDate taskUpdatedAt;

    @Enumerated(EnumType.STRING)
    @JsonProperty("status")
    private Status status;

    public void validateWarehouseTaskStatus() {
        if (!status.equals(Status.RECEPTION_AREA) && !status.equals(Status.RELEASE_AREA)) {
            throw new IncorrectStatusException();
        }
    }

    public void approveTask(){
        this.approvalStatus = ApprovalStatus.APPROVED;
    }

    public void updateWith(WarehouseTask warehouseTask) {
        this.productId = warehouseTask.getProductId();
        this.quantity = warehouseTask.getQuantity();
        this.approvalStatus = ApprovalStatus.NOT_APPROVED;
        this.taskUpdatedAt = LocalDate.now();
        this.status = warehouseTask.getStatus();
    }
}