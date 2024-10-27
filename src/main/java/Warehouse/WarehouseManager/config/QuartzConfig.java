package Warehouse.WarehouseManager.config;

import Warehouse.WarehouseManager.job.ShareTheProductsReportJob;
import Warehouse.WarehouseManager.job.StartListenerJob;
import Warehouse.WarehouseManager.job.StopListenerJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.Date;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail startListenerJobDetail(){
        return JobBuilder.newJob(StartListenerJob.class)
                .withIdentity("startListener")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger startListenerTrigger(JobDetail startListenerJobDetail){
        return TriggerBuilder.newTrigger()
                .forJob(startListenerJobDetail)
                .withIdentity("startListener")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 * * ?"))
                .build();
    }

    @Bean
    public JobDetail stopListenerJobDetail(){
        return JobBuilder.newJob(StopListenerJob.class)
                .withIdentity("stopListener")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger stopListenerTrigger(JobDetail stopListenerJobDetail){
        return TriggerBuilder.newTrigger()
                .forJob(stopListenerJobDetail)
                .withIdentity("stopListener")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 5 0 * * ?"))
                .build();
    }

    @Bean
    public JobDetail shareTheProductsReportJobDetail(){
        return JobBuilder.newJob(ShareTheProductsReportJob.class)
                .withIdentity("shareTheProductsReport")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger shareTheProductsReportTrigger(JobDetail shareTheProductsReportJobDetail){
        return TriggerBuilder.newTrigger()
                .forJob(shareTheProductsReportJobDetail)
                .withIdentity("shareTheProductsReport")
                .startAt(Date.from(Instant.now()))
                .withSchedule(CronScheduleBuilder.cronSchedule("0 2 0 * * ?"))
                .build();
    }

}
