package com.wms;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ErdPdfGeneratorTest {

    @Test
    @DisplayName("Generate 6-Table Architecture ERD PDF document")
    void generateErdPdf() throws Exception {
        byte[] pdfBytes = buildErdPdf();
        assertTrue(pdfBytes.length > 1000, "ERD PDF should have substantial content");
        assertEquals("%PDF-", new String(pdfBytes, 0, 5), "Must be valid PDF header");

        // Save to workspace root
        try (FileOutputStream fos = new FileOutputStream("ERD.pdf")) {
            fos.write(pdfBytes);
        }

        // Save to artifact directory
        String artifactPath = "C:/Users/User/.gemini/antigravity-acp/brain/3e1e1290-7042-478a-a380-ecc26c752103/ERD.pdf";
        try (FileOutputStream fos = new FileOutputStream(artifactPath)) {
            fos.write(pdfBytes);
        }

        System.out.println("ERD PDF successfully generated at: ERD.pdf and " + artifactPath);
    }

    private byte[] buildErdPdf() throws IOException {
        StringBuilder cb = new StringBuilder(32768);

        final float margin = 32f;
        final float contentW = 595.28f - (2 * margin); // ~531 pt

        // --- 1. Header Banner ---
        appendRect(cb, margin, 792, contentW, 26, 0.12f, 0.23f, 0.54f, true, false); // #1e3a8a
        appendText(cb, "WORKSHOP MANAGEMENT SYSTEM (WMS) - POSTGRESQL 18", margin + 10, 800, "/F2", 11f, 1f, 1f, 1f);

        appendText(cb, "Entity Relationship Diagram (ERD) - Consolidated 6-Table Architecture",
                margin, 772, "/F2", 13f, 0.06f, 0.09f, 0.16f);

        String genDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
        String meta = "Strict Limit: Exactly 6 Tables | Base: Malaysia (RM / 6% SST) | Security: BCrypt | Date: " + genDate;
        appendText(cb, meta, margin, 758, "/F1", 8f, 0.35f, 0.40f, 0.48f);

        appendLine(cb, margin, 750, margin + contentW, 750, 0.80f, 0.84f, 0.89f, 1f);

        // --- 2. Architecture Overview & Consolidation Summary ---
        appendText(cb, "1. CONSOLIDATION ARCHITECTURE (9 TABLES -> EXACTLY 6 NORMALIZED TABLES)", margin, 736, "/F2", 10f, 0.12f, 0.23f, 0.54f);

        float tableY = 720f;
        float rowH = 15f;
        appendRect(cb, margin, tableY, contentW, rowH, 0.12f, 0.23f, 0.54f, true, false);
        appendText(cb, "Consolidated Table", margin + 6, tableY + 4, "/F2", 8f, 1f, 1f, 1f);
        appendText(cb, "Source Entities", margin + 115, tableY + 4, "/F2", 8f, 1f, 1f, 1f);
        appendText(cb, "Consolidation Strategy & Relational Role", margin + 230, tableY + 4, "/F2", 8f, 1f, 1f, 1f);

        String[][] mapping = {
            {"1. customers", "customers", "Primary client profiles and contact information for vehicle ownership."},
            {"2. vehicles", "vehicles (no VIN)", "Fleet registry without VIN requirement for rapid counter onboarding."},
            {"3. staff", "staff", "Workshop accounts, role authorization, and BCrypt encrypted passwords."},
            {"4. catalog_items", "service_types + parts", "Unified catalog with item_type ('SERVICE'/'PART'), stock, and maintenance intervals."},
            {"5. job_orders", "job_orders + invoices", "Unified work order lifecycle merged with 1:1 billing, tax (6% SST), and payment."},
            {"6. job_items", "job_services + job_parts", "Unified line items table referencing job_orders & catalog_items by item_type."}
        };

        float curY = tableY - rowH;
        boolean alt = false;
        for (String[] row : mapping) {
            float bg = alt ? 0.96f : 1.0f;
            appendRect(cb, margin, curY, contentW, rowH, bg, bg, bg, true, true);
            appendRect(cb, margin, curY, contentW, rowH, 0.82f, 0.85f, 0.90f, false, true);

            appendText(cb, row[0], margin + 6, curY + 4, "/F2", 7.5f, 0.06f, 0.09f, 0.16f);
            appendText(cb, row[1], margin + 115, curY + 4, "/F1", 7.5f, 0.20f, 0.25f, 0.35f);
            appendText(cb, row[2], margin + 230, curY + 4, "/F1", 7.2f, 0.30f, 0.35f, 0.42f);

            curY -= rowH;
            alt = !alt;
        }

        // --- 3. Relational Foreign Key Matrix ---
        curY -= 10f;
        appendText(cb, "2. RELATIONAL CARDINALITY & FOREIGN KEY CONSTRAINTS", margin, curY, "/F2", 10f, 0.12f, 0.23f, 0.54f);

        curY -= 16f;
        appendRect(cb, margin, curY, contentW, rowH, 0.12f, 0.23f, 0.54f, true, false);
        appendText(cb, "Relationship", margin + 6, curY + 4, "/F2", 8f, 1f, 1f, 1f);
        appendText(cb, "Cardinality", margin + 140, curY + 4, "/F2", 8f, 1f, 1f, 1f);
        appendText(cb, "Foreign Key Definition", margin + 220, curY + 4, "/F2", 8f, 1f, 1f, 1f);
        appendText(cb, "Integrity Rule", margin + 410, curY + 4, "/F2", 8f, 1f, 1f, 1f);

        String[][] fks = {
            {"customers -> vehicles", "1 to 0..N", "vehicles.customer_id -> customers.customer_id", "ON DELETE CASCADE"},
            {"vehicles -> job_orders", "1 to 0..N", "job_orders.vehicle_id -> vehicles.vehicle_id", "RESTRICT"},
            {"staff -> job_orders (advisor)", "1 to 0..N", "job_orders.advisor_id -> staff.staff_id", "RESTRICT"},
            {"staff -> job_orders (mechanic)", "1 to 0..N", "job_orders.mechanic_id -> staff.staff_id", "SET NULL / RESTRICT"},
            {"job_orders -> job_items", "1 to 0..N", "job_items.job_id -> job_orders.job_id", "ON DELETE CASCADE"},
            {"catalog_items -> job_items", "1 to 0..N", "job_items.item_id -> catalog_items.item_id", "RESTRICT"}
        };

        curY -= rowH;
        alt = false;
        for (String[] fk : fks) {
            float bg = alt ? 0.96f : 1.0f;
            appendRect(cb, margin, curY, contentW, rowH, bg, bg, bg, true, true);
            appendRect(cb, margin, curY, contentW, rowH, 0.82f, 0.85f, 0.90f, false, true);

            appendText(cb, fk[0], margin + 6, curY + 4, "/F2", 7.5f, 0.06f, 0.09f, 0.16f);
            appendText(cb, fk[1], margin + 140, curY + 4, "/F1", 7.5f, 0.20f, 0.25f, 0.35f);
            appendText(cb, fk[2], margin + 220, curY + 4, "/F1", 7.2f, 0.12f, 0.23f, 0.54f);
            appendText(cb, fk[3], margin + 410, curY + 4, "/F2", 7.2f, 0.02f, 0.47f, 0.34f);

            curY -= rowH;
            alt = !alt;
        }

        // --- 4. Database Schema Tables (Top 3 Tables) ---
        curY -= 10f;
        appendText(cb, "3. TABLE SCHEMAS & COLUMN SPECIFICATIONS", margin, curY, "/F2", 10f, 0.12f, 0.23f, 0.54f);

        curY -= 14f;
        // Table 1: customers & Table 2: vehicles (Side by Side)
        float colW = (contentW - 10f) / 2f;
        drawSchemaBox(cb, margin, curY - 70f, colW, 70f, "1. customers (Client Registry)", new String[][]{
            {"customer_id", "BIGSERIAL (PK)", "Unique customer ID"},
            {"full_name", "VARCHAR(150)", "Customer legal name"},
            {"phone", "VARCHAR(30)", "Contact phone (Index)"},
            {"email, address", "VARCHAR", "Optional contact details"},
            {"created_at", "TIMESTAMP", "Registration timestamp"}
        });

        drawSchemaBox(cb, margin + colW + 10f, curY - 70f, colW, 70f, "2. vehicles (Fleet Registry - No VIN)", new String[][]{
            {"vehicle_id", "BIGSERIAL (PK)", "Unique vehicle ID"},
            {"customer_id", "BIGINT (FK)", "Owner (Cascade)"},
            {"plate_no", "VARCHAR(20) (UK)", "Registration number"},
            {"make, model, year", "VARCHAR / INT", "Car specifications"},
            {"current_mileage", "INT", "Intake odometer (km)"}
        });

        curY -= 78f;
        // Table 3: staff & Table 4: catalog_items (Side by Side)
        drawSchemaBox(cb, margin, curY - 75f, colW, 75f, "3. staff (Personnel & Roles)", new String[][]{
            {"staff_id", "BIGSERIAL (PK)", "Staff identifier"},
            {"username", "VARCHAR(50) (UK)", "Login username"},
            {"password_hash", "VARCHAR(255)", "BCrypt salted hash"},
            {"role", "VARCHAR(30)", "COUNTER | MECHANIC | MANAGER"},
            {"active, phone", "BOOL / VARCHAR", "Status flag & phone"}
        });

        drawSchemaBox(cb, margin + colW + 10f, curY - 75f, colW, 75f, "4. catalog_items (Unified Catalog)", new String[][]{
            {"item_id", "BIGSERIAL (PK)", "Catalog identifier"},
            {"item_type", "VARCHAR(20)", "'SERVICE' or 'PART'"},
            {"item_code", "VARCHAR(60) (UK)", "SKU / Package Code"},
            {"unit_price, cost_price", "DECIMAL(10,2)", "Price in RM"},
            {"stock_qty, interval_km", "INT", "Inventory / Service km"}
        });

        curY -= 83f;
        // Table 5: job_orders (Merged Work Order & Billing)
        drawSchemaBox(cb, margin, curY - 95f, contentW, 95f, "5. job_orders (Consolidated Work Order Lifecycle & 1:1 Invoicing)", new String[][]{
            {"job_id (PK)", "BIGSERIAL", "Work order & invoice identifier"},
            {"vehicle_id, advisor_id, mechanic_id (FKs)", "BIGINT", "Vehicle, intake advisor, and assigned technician bay"},
            {"date_in, date_out, mileage_in", "TIMESTAMP / INT", "Vehicle intake, completion timestamps, and odometer"},
            {"complaint, diagnosis, status", "VARCHAR", "Intake issue, findings, and ticket status (PENDING..INVOICED)"},
            {"next_service_date, next_service_km", "DATE / INT", "Projected maintenance recommendations (+6 mos / +10,000 km)"},
            {"invoice_date, subtotal, tax, discount, total", "DATE / DECIMAL", "Consolidated billing fields: 6% SST, discounts, total in RM"},
            {"payment_status, payment_method, paid_at", "VARCHAR / TIMESTAMP", "Settlement state (PENDING / PAID), method (CASH/CARD/DUITNOW)"}
        });

        curY -= 103f;
        // Table 6: job_items (Unified Line Items)
        drawSchemaBox(cb, margin, curY - 55f, contentW, 55f, "6. job_items (Unified Services Rendered & Parts Consumed)", new String[][]{
            {"job_item_id (PK)", "BIGSERIAL", "Line item identifier"},
            {"job_id (FK), item_id (FK)", "BIGINT", "Linked work order (Cascade) and catalog reference"},
            {"item_type, quantity, unit_price", "VARCHAR / INT / DECIMAL", "Differentiator ('SERVICE'/'PART'), quantity, and RM rate"},
            {"notes", "VARCHAR(255)", "Mechanic remarks or installation observations"}
        });

        // --- 5. Footer ---
        appendLine(cb, margin, 30, margin + contentW, 30, 0.80f, 0.84f, 0.89f, 1f);
        appendText(cb, "Workshop Management System (WMS) | Official 6-Table Relational Database Specification | Confidential",
                margin + 40, 20, "/F1", 7.5f, 0.40f, 0.45f, 0.53f);

        return assemblePdf(cb.toString());
    }

    private void drawSchemaBox(StringBuilder cb, float x, float y, float w, float h, String title, String[][] cols) {
        // Box header
        appendRect(cb, x, y + h - 14f, w, 14f, 0.12f, 0.23f, 0.54f, true, false);
        appendText(cb, title, x + 5, y + h - 10f, "/F2", 7.5f, 1f, 1f, 1f);

        // Box body
        appendRect(cb, x, y, w, h - 14f, 0.98f, 0.99f, 1.0f, true, false);
        appendRect(cb, x, y, w, h, 0.80f, 0.84f, 0.89f, false, true);

        float textY = y + h - 23f;
        for (String[] col : cols) {
            appendText(cb, col[0], x + 5, textY, "/F2", 6.8f, 0.06f, 0.09f, 0.16f);
            appendText(cb, col[1], x + 90, textY, "/F1", 6.8f, 0.12f, 0.23f, 0.54f);
            if (col.length > 2) {
                appendText(cb, col[2], x + 165, textY, "/F1", 6.5f, 0.35f, 0.40f, 0.48f);
            }
            textY -= 9.5f;
        }
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

        // Header
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
}
