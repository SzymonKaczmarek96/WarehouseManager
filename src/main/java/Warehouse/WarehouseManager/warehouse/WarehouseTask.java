package Warehouse.WarehouseManager.warehouse;

import Warehouse.WarehouseManager.enums.ApprovalStatus;
import Warehouse.WarehouseManager.enums.CompletionStatus;
import Warehouse.WarehouseManager.enums.Status;
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

public class WarehouseTask implements Serializable{
    @JsonProperty("task_id")
    private long id;

    @JsonProperty("product_id")
    private long productId;

    @JsonProperty("quantity")
    private long quantity;

    @Enumerated(EnumType.STRING)
    @JsonProperty("approval_status")
    private ApprovalStatus approvalStatus = ApprovalStatus.NOT_APPROVED;

    @JsonProperty("task_created_ad")
    private LocalDate taskCreatedAt;

    @Enumerated(EnumType.STRING)
    @JsonProperty("completion_status")
    private CompletionStatus completionStatus = CompletionStatus.NOT_DONE;

    @Enumerated(EnumType.STRING)
    @JsonProperty("status")
    private Status status;

}
