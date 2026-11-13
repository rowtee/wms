package com.wms.service;

import com.wms.model.MonthlyReport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 100% Native Java PDF Report Generation Service for Workshop Management System.
 * Zero external third-party PDF library dependencies required.
 * Generates standards-compliant PDF 1.4 documents.
 */
public class PdfReportService {

    private static final DecimalFormat CURRENCY_FMT = new DecimalFormat("#,##0.00");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    public void generateMonthlyReportPdf(MonthlyReport report, OutputStream out) throws Exception {
        byte[] pdfData = buildPdfBytes(report);
        out.write(pdfData);
        out.flush();
    }

    private byte[] buildPdfBytes(MonthlyReport report) throws IOException {
        StringBuilder cb = new StringBuilder(16384);

        // Page setup: A4 dimensions: 595 x 842 points
        final float pageW = 595f;
        final float margin = 36f;
        final float contentW = pageW - (2 * margin); // 523 pt

        // --- 1. Header Banner ---
        // Top banner accent
        appendRect(cb, margin, 786, contentW, 26, 0.12f, 0.23f, 0.54f, true, false); // #1e3a8a
        appendText(cb, "WORKSHOP MANAGEMENT SYSTEM (WMS)", margin + 10, 794, "/F2", 12, 1f, 1f, 1f);

        // Title & metadata
        String monthName = java.time.Month.of(report.getMonth()).name();
        appendText(cb, "Executive Monthly Performance & Financial Report: " + monthName + " " + report.getYear(),
                margin, 765, "/F2", 13, 0.06f, 0.09f, 0.16f);

        String meta = "Base Country: Malaysia (MYR)  |  Statutory Tax: SST (6%)  |  Generated: " + LocalDateTime.now().format(DATE_FMT);
        appendText(cb, meta, margin, 750, "/F1", 8.5f, 0.40f, 0.45f, 0.53f);

        appendLine(cb, margin, 740, margin + contentW, 740, 0.80f, 0.84f, 0.89f, 1f);

        // --- 2. Financial Overview KPI Grid ---
        appendText(cb, "1. FINANCIAL & OPERATIONS OVERVIEW (MALAYSIA BASE)", margin, 725, "/F2", 10.5f, 0.12f, 0.23f, 0.54f);

        // Top 4 KPI Cards
        float cardW = (contentW - 18) / 4f; // ~126 pt
        float cardH = 48f;
        float yKpi1 = 670f;

        drawKpiCard(cb, margin, yKpi1, cardW, cardH, "TOTAL SALES (RM)", "RM " + formatAmount(report.getTotalSales()), "Gross Settled Revenue", 0.02f, 0.47f, 0.34f);
        drawKpiCard(cb, margin + cardW + 6, yKpi1, cardW, cardH, "SST COLLECTED (6%)", "RM " + formatAmount(report.getTotalTaxCollected()), "Statutory Tax", 0.12f, 0.23f, 0.54f);
        drawKpiCard(cb, margin + (cardW + 6) * 2, yKpi1, cardW, cardH, "LABOR REVENUE", "RM " + formatAmount(report.getTotalLaborRevenue()), "Technician Services", 0.12f, 0.23f, 0.54f);
        drawKpiCard(cb, margin + (cardW + 6) * 3, yKpi1, cardW, cardH, "PARTS REVENUE", "RM " + formatAmount(report.getTotalPartsRevenue()), "Inventory Parts", 0.43f, 0.16f, 0.85f);

        // Secondary 4 KPI Cards
        float yKpi2 = 616f;
        drawKpiCard(cb, margin, yKpi2, cardW, cardH, "VEHICLES SERVICED", String.valueOf(report.getTotalVehiclesServiced()), "Cars in bays", 0.71f, 0.33f, 0.04f);
        drawKpiCard(cb, margin + cardW + 6, yKpi2, cardW, cardH, "NEW CUSTOMERS", "+" + report.getNewCustomersRegistered(), "Client registration", 0.02f, 0.47f, 0.34f);
        drawKpiCard(cb, margin + (cardW + 6) * 2, yKpi2, cardW, cardH, "COMPLETED ORDERS", String.valueOf(report.getCompletedOrders()), "Fulfilled work orders", 0.12f, 0.23f, 0.54f);
        drawKpiCard(cb, margin + (cardW + 6) * 3, yKpi2, cardW, cardH, "INVOICED JOBS", String.valueOf(report.getInvoicedOrders()), "Billed & closed", 0.12f, 0.23f, 0.54f);

        // --- 3. Mechanic Productivity Table ---
        float yMechTitle = 595f;
        appendText(cb, "2. MECHANIC WORKLOAD & PRODUCTIVITY RANKING", margin, yMechTitle, "/F2", 10.5f, 0.12f, 0.23f, 0.54f);

        float tableY = 575f;
        float rowH = 18f;

        // Header row
        appendRect(cb, margin, tableY, contentW, rowH, 0.12f, 0.23f, 0.54f, true, false);
        appendText(cb, "Mechanic Name", margin + 8, tableY + 5, "/F2", 8.5f, 1f, 1f, 1f);
        appendText(cb, "Total Assigned Jobs", margin + 180, tableY + 5, "/F2", 8.5f, 1f, 1f, 1f);
        appendText(cb, "Completed Jobs", margin + 300, tableY + 5, "/F2", 8.5f, 1f, 1f, 1f);
        appendText(cb, "Completion Rate (%)", margin + 420, tableY + 5, "/F2", 8.5f, 1f, 1f, 1f);

        float currentY = tableY - rowH;
        List<Map<String, Object>> mechanics = report.getMechanicPerformance();
        if (mechanics != null && !mechanics.isEmpty()) {
            boolean alt = false;
            for (Map<String, Object> m : mechanics) {
                float bg = alt ? 0.97f : 1.0f;
                appendRect(cb, margin, currentY, contentW, rowH, bg, bg, bg, true, true);
                appendRect(cb, margin, currentY, contentW, rowH, 0.80f, 0.84f, 0.89f, false, true);

                String name = String.valueOf(m.get("name"));
                int total = ((Number) m.getOrDefault("totalJobs", 0)).intValue();
                int completed = ((Number) m.getOrDefault("completedJobs", 0)).intValue();
                int rate = total > 0 ? (int) Math.round(((double) completed / total) * 100) : 0;

                appendText(cb, name, margin + 8, currentY + 5, "/F2", 8.5f, 0.06f, 0.09f, 0.16f);
                appendText(cb, total + " vehicles", margin + 180, currentY + 5, "/F1", 8.5f, 0.2f, 0.2f, 0.2f);
                appendText(cb, completed + " completed", margin + 300, currentY + 5, "/F1", 8.5f, 0.02f, 0.47f, 0.34f);
                appendText(cb, rate + "%", margin + 420, currentY + 5, "/F2", 8.5f, 0.06f, 0.09f, 0.16f);

                currentY -= rowH;
                alt = !alt;
            }
        } else {
            appendRect(cb, margin, currentY, contentW, rowH, 1f, 1f, 1f, true, true);
            appendText(cb, "No mechanic productivity records found for this period.", margin + 120, currentY + 5, "/F1", 8.5f, 0.4f, 0.4f, 0.4f);
            currentY -= rowH;
        }

        // --- 4. Top Performing Workshop Packages ---
        float ySvcTitle = currentY - 15;
        appendText(cb, "3. TOP WORKSHOP SERVICE PACKAGES", margin, ySvcTitle, "/F2", 10.5f, 0.12f, 0.23f, 0.54f);

        float svcTableY = ySvcTitle - 20;
        appendRect(cb, margin, svcTableY, contentW, rowH, 0.12f, 0.23f, 0.54f, true, false);
        appendText(cb, "Service Package Description", margin + 8, svcTableY + 5, "/F2", 8.5f, 1f, 1f, 1f);
        appendText(cb, "Frequency (Orders)", margin + 280, svcTableY + 5, "/F2", 8.5f, 1f, 1f, 1f);
        appendText(cb, "Total Revenue Generated (RM)", margin + 400, svcTableY + 5, "/F2", 8.5f, 1f, 1f, 1f);

        float svcY = svcTableY - rowH;
        List<Map<String, Object>> services = report.getPopularServices();
        if (services != null && !services.isEmpty()) {
            boolean alt = false;
            for (Map<String, Object> s : services) {
                float bg = alt ? 0.97f : 1.0f;
                appendRect(cb, margin, svcY, contentW, rowH, bg, bg, bg, true, true);
                appendRect(cb, margin, svcY, contentW, rowH, 0.80f, 0.84f, 0.89f, false, true);

                String name = String.valueOf(s.get("name"));
                int count = ((Number) s.getOrDefault("usageCount", 0)).intValue();
                Object totalChargeObj = s.get("totalCharge");
                String totalChargeStr = totalChargeObj instanceof BigDecimal ?
                        formatAmount((BigDecimal) totalChargeObj) : String.valueOf(totalChargeObj);

                appendText(cb, name, margin + 8, svcY + 5, "/F2", 8.5f, 0.06f, 0.09f, 0.16f);
                appendText(cb, count + " orders", margin + 280, svcY + 5, "/F1", 8.5f, 0.2f, 0.2f, 0.2f);
                appendText(cb, "RM " + totalChargeStr, margin + 400, svcY + 5, "/F2", 8.5f, 0.02f, 0.47f, 0.34f);

                svcY -= rowH;
                alt = !alt;
            }
        } else {
            appendRect(cb, margin, svcY, contentW, rowH, 1f, 1f, 1f, true, true);
            appendText(cb, "No service packages billed during this period.", margin + 140, svcY + 5, "/F1", 8.5f, 0.4f, 0.4f, 0.4f);
            svcY -= rowH;
        }

        // --- 5. Sign-off Footer ---
        appendLine(cb, margin, 50, margin + contentW, 50, 0.80f, 0.84f, 0.89f, 1f);
        appendText(cb, "Authorized by General Management  |  Workshop Governance & Auditing System  |  Confidential",
                margin + 60, 38, "/F1", 8f, 0.40f, 0.45f, 0.53f);

        // Assemble standard PDF document structure
        return assemblePdf(cb.toString());
    }

    private void drawKpiCard(StringBuilder cb, float x, float y, float w, float h,
                             String title, String val, String sub, float vr, float vg, float vb) {
        // Background card box
        appendRect(cb, x, y, w, h, 0.96f, 0.97f, 0.98f, true, false);
        appendRect(cb, x, y, w, h, 0.80f, 0.84f, 0.89f, false, true);

        // Title
        appendText(cb, title, x + 6, y + h - 12, "/F1", 7f, 0.40f, 0.45f, 0.53f);
        // Value
        appendText(cb, val, x + 6, y + h - 26, "/F2", 11.5f, vr, vg, vb);
        // Subtext
        appendText(cb, sub, x + 6, y + 6, "/F1", 6.5f, 0.50f, 0.55f, 0.63f);
    }

    private void appendRect(StringBuilder cb, float x, float y, float w, float h,
                            float r, float g, float b, boolean fill, boolean stroke) {
        if (fill) {
            cb.append(String.format(java.util.Locale.US, "%.2f %.2f %.2f rg\n", r, g, b));
            cb.append(String.format(java.util.Locale.US, "%.2f %.2f %.2f %.2f re f\n", x, y, w, h));
        }
        if (stroke) {
            cb.append(String.format(java.util.Locale.US, "0.5 w %.2f %.2f %.2f RG\n", r, g, b));
            cb.append(String.format(java.util.Locale.US, "%.2f %.2f %.2f %.2f re S\n", x, y, w, h));
        }
    }

    private void appendLine(StringBuilder cb, float x1, float y1, float x2, float y2,
                            float r, float g, float b, float width) {
        cb.append(String.format(java.util.Locale.US, "%.2f w %.2f %.2f %.2f RG\n", width, r, g, b));
        cb.append(String.format(java.util.Locale.US, "%.2f %.2f m %.2f %.2f l S\n", x1, y1, x2, y2));
    }

    private void appendText(StringBuilder cb, String text, float x, float y,
                            String font, float size, float r, float g, float b) {
        if (text == null) text = "";
        String escaped = escapePdf(text);
        cb.append("BT\n");
        cb.append(String.format(java.util.Locale.US, "%.2f %.2f %.2f rg\n", r, g, b));
        cb.append(String.format(java.util.Locale.US, "%s %.2f Tf\n", font, size));
        cb.append(String.format(java.util.Locale.US, "%.2f %.2f Td\n", x, y));
        cb.append("(").append(escaped).append(") Tj\n");
        cb.append("ET\n");
    }

    private String escapePdf(String s) {
        return s.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }

    private byte[] assemblePdf(String streamContent) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();

        // 1. Header
        write(out, "%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n");

        // Object 1: Catalog
        offsets.add(out.size());
        write(out, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        // Object 2: Pages
        offsets.add(out.size());
        write(out, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

        // Object 3: Page (A4: 595.28 x 841.89)
        offsets.add(out.size());
        write(out, "3 0 obj\n<< /Type /Page /Parent 2 0 R\n" +
                "/MediaBox [0 0 595.28 841.89]\n" +
                "/Resources << /Font << /F1 4 0 R /F2 5 0 R >> >>\n" +
                "/Contents 6 0 R\n>>\nendobj\n");

        // Object 4: Font F1 (Helvetica Regular)
        offsets.add(out.size());
        write(out, "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>\nendobj\n");

        // Object 5: Font F2 (Helvetica Bold)
        offsets.add(out.size());
        write(out, "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>\nendobj\n");

        // Object 6: Stream Contents
        byte[] streamBytes = streamContent.getBytes(StandardCharsets.ISO_8859_1);
        offsets.add(out.size());
        write(out, "6 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n");
        out.write(streamBytes);
        write(out, "\nendstream\nendobj\n");

        // XRef Table
        int xrefPos = out.size();
        write(out, "xref\n0 " + (offsets.size() + 1) + "\n");
        write(out, "0000000000 65535 f \n");
        for (int off : offsets) {
            write(out, String.format(java.util.Locale.US, "%010d 00000 n \n", off));
        }

        // Trailer
        write(out, "trailer\n<< /Size " + (offsets.size() + 1) + " /Root 1 0 R >>\n");
        write(out, "startxref\n" + xrefPos + "\n%%EOF\n");

        return out.toByteArray();
    }

    private void write(ByteArrayOutputStream out, String s) throws IOException {
        out.write(s.getBytes(StandardCharsets.US_ASCII));
    }

    private static String formatAmount(BigDecimal amount) {
        if (amount == null) return "0.00";
        return CURRENCY_FMT.format(amount);
    }
}
