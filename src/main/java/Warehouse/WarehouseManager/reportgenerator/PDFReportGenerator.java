package Warehouse.WarehouseManager.reportgenerator;

import Warehouse.WarehouseManager.consumer.ProductConsumer;
import Warehouse.WarehouseManager.product.ProductDto;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class PDFReportGenerator {

    private ProductConsumer productConsumer;

    @Autowired
    public PDFReportGenerator(ProductConsumer productConsumer) {
        this.productConsumer = productConsumer;
    }

    private void generateProductReport(List<ProductDto> products) {
        try {

            PdfWriter writer = new PdfWriter(generateUniqueReportName());
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("Product Report")
                    .setBold()
                    .setFontSize(18));

            Table table = new Table(3);
            table.addHeaderCell("Product ID");
            table.addHeaderCell("Product Name");
            table.addHeaderCell("Product Size");

            for (ProductDto product : products) {
                table.addCell(product.id().toString());
                table.addCell(product.name());
                table.addCell(product.size().name());
            }
            document.add(table);
            document.close();
            System.out.println("PDF created at: " + generateUniqueReportName());

        } catch (IOException message) {
            System.out.println(message);
        }
    }

    public void shareTheProductsReport(){
        generateProductReport(productConsumer.retrieveAllProductAddedAtDay());
    }

    public String generateUniqueReportName() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        return "report_" + timestamp + ".pdf";
    }


}
