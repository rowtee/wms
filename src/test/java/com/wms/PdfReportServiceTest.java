package com.wms;

import com.wms.dao.InvoiceDao;
import com.wms.model.MonthlyReport;
import com.wms.service.PdfReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PdfReportServiceTest {

    @Test
    @DisplayName("Generate PDF report stream successfully with valid PDF header and bytes")
    void testGeneratePdfReport() throws Exception {
        PdfReportService service = new PdfReportService();

        MonthlyReport report = new MonthlyReport();
        report.setYear(2026);
        report.setMonth(11);
        report.setTotalOrders(15);
        report.setCompletedOrders(12);
        report.setInvoicedOrders(10);
        report.setTotalSales(new BigDecimal("12450.00"));
        report.setTotalLaborRevenue(new BigDecimal("6200.00"));
        report.setTotalPartsRevenue(new BigDecimal("5502.83"));
        report.setTotalTaxCollected(new BigDecimal("747.00"));
        report.setTotalVehiclesServiced(14);
        report.setNewCustomersRegistered(5);

        // Mock mechanics
        ArrayList<Map<String, Object>> mechanics = new ArrayList<>();
        Map<String, Object> m1 = new HashMap<>();
        m1.put("name", "Mike Ramirez");
        m1.put("totalJobs", 8);
        m1.put("completedJobs", 7);
        mechanics.add(m1);

        Map<String, Object> m2 = new HashMap<>();
        m2.put("name", "Dave Sterling");
        m2.put("totalJobs", 6);
        m2.put("completedJobs", 5);
        mechanics.add(m2);
        report.setMechanicPerformance(mechanics);

        // Mock services
        ArrayList<Map<String, Object>> services = new ArrayList<>();
        Map<String, Object> s1 = new HashMap<>();
        s1.put("name", "Standard Oil & Filter Service");
        s1.put("usageCount", 10);
        s1.put("totalCharge", new BigDecimal("450.00"));
        services.add(s1);
        report.setPopularServices(services);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.generateMonthlyReportPdf(report, out);

        byte[] pdfBytes = out.toByteArray();
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500, "PDF should not be empty, length: " + pdfBytes.length);

        // Verify PDF Magic Bytes (%PDF-)
        String header = new String(pdfBytes, 0, 5);
        assertEquals("%PDF-", header, "File must start with standard PDF magic header");
    }

    @Test
    @DisplayName("Generate PDF report directly from InvoiceDao data")
    void testGeneratePdfFromDao() throws Exception {
        InvoiceDao invoiceDao = new InvoiceDao();
        MonthlyReport report = invoiceDao.getMonthlyReport(2026, 11);

        assertNotNull(report);
        PdfReportService service = new PdfReportService();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.generateMonthlyReportPdf(report, out);

        byte[] pdfBytes = out.toByteArray();
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500);
        assertEquals("%PDF-", new String(pdfBytes, 0, 5));
    }
}
