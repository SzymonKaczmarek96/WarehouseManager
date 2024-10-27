package Warehouse.WarehouseManager.job;

import Warehouse.WarehouseManager.consumer.ProductConsumer;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
public class StopListenerJob implements Job {
    private ProductConsumer productConsumer;

    public StopListenerJob(ProductConsumer productConsumer) {
        this.productConsumer = productConsumer;
    }

    public void execute(JobExecutionContext context){
        productConsumer.stopListener("id-1");
    }
}
