package Warehouse.WarehouseManager.warehouse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WarehouseTasks implements Serializable{
    private List<WarehouseTask> warehouseTaskList = new ArrayList<>();
}
