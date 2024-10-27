package Warehouse.WarehouseManager.job;

import Warehouse.WarehouseManager.reportgenerator.PDFReportGenerator;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShareTheProductsReportJob implements Job {
    private PDFReportGenerator pdfReportGenerator;

    @Autowired
    public ShareTheProductsReportJob(PDFReportGenerator pdfReportGenerator) {
        this.pdfReportGenerator = pdfReportGenerator;
    }

    @Override
    public void execute(JobExecutionContext context) {
        pdfReportGenerator.shareTheProductsReport();
    }
}
