package com.wms.dao;

import com.wms.config.DatabaseConfig;
import com.wms.model.Invoice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InvoiceDao {
    private static final Logger logger = LoggerFactory.getLogger(InvoiceDao.class);

    private static final String SELECT_BASE =
        "SELECT j.job_id AS invoice_id, j.job_id, j.invoice_date, j.subtotal, j.tax, j.discount, j.total, " +
        "j.payment_status, j.payment_method, j.paid_at, " +
        "v.plate_no, c.full_name AS customer_name, c.phone AS customer_phone " +
        "FROM job_orders j " +
        "JOIN vehicles v ON j.vehicle_id = v.vehicle_id " +
        "JOIN customers c ON v.customer_id = c.customer_id ";

    public List<Invoice> findAll() {
        String sql = SELECT_BASE + "WHERE j.status = 'INVOICED' OR j.subtotal > 0 OR j.total > 0 ORDER BY j.job_id DESC";
        List<Invoice> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving invoices", e);
        }
        return list;
    }

    public Optional<Invoice> findByJobId(Long jobId) {
        String sql = SELECT_BASE + "WHERE j.job_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, jobId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding invoice for job ID: {}", jobId, e);
        }
        return Optional.empty();
    }

    public Long createInvoiceForJob(Long jobId, BigDecimal subtotal, BigDecimal tax, BigDecimal discount, BigDecimal total) {
        String sql = "UPDATE job_orders SET invoice_date = CURRENT_DATE, subtotal = ?, tax = ?, discount = ?, total = ?, " +
                     "payment_status = 'PENDING', status = 'INVOICED' WHERE job_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, subtotal);
            stmt.setBigDecimal(2, tax);
            stmt.setBigDecimal(3, discount);
            stmt.setBigDecimal(4, total);
            stmt.setLong(5, jobId);
            int updated = stmt.executeUpdate();
            if (updated > 0) {
                return jobId;
            }
        } catch (SQLException e) {
            logger.error("Error creating invoice for job ID: {}", jobId, e);
        }
        return null;
    }

    public boolean markPaid(Long invoiceId, String paymentMethod) {
        String sql = "UPDATE job_orders SET payment_status = 'PAID', payment_method = ?, paid_at = CURRENT_TIMESTAMP WHERE job_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, paymentMethod != null ? paymentMethod : "CASH");
            stmt.setLong(2, invoiceId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error marking invoice ID {} as paid", invoiceId, e);
        }
        return false;
    }

    public com.wms.model.MonthlyReport getMonthlyReport(int year, int month) {
        com.wms.model.MonthlyReport rep = new com.wms.model.MonthlyReport();
        rep.setYear(year);
        rep.setMonth(month);

        try (Connection conn = DatabaseConfig.getConnection()) {
            // 1. Total jobs count and completed jobs count
            String jobSql = "SELECT COUNT(*) AS total_orders, " +
                            "COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) AS completed_orders, " +
                            "COUNT(CASE WHEN status = 'INVOICED' THEN 1 END) AS invoiced_orders, " +
                            "COUNT(DISTINCT vehicle_id) AS vehicles_serviced " +
                            "FROM job_orders " +
                            "WHERE EXTRACT(YEAR FROM date_in) = ? AND EXTRACT(MONTH FROM date_in) = ?";
            try (PreparedStatement stmt = conn.prepareStatement(jobSql)) {
                stmt.setInt(1, year);
                stmt.setInt(2, month);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        rep.setTotalOrders(rs.getInt("total_orders"));
                        rep.setCompletedOrders(rs.getInt("completed_orders"));
                        rep.setInvoicedOrders(rs.getInt("invoiced_orders"));
                        rep.setTotalVehiclesServiced(rs.getInt("vehicles_serviced"));
                    }
                }
            }

            // 2. Financial totals from invoices (merged into job_orders)
            String invSql = "SELECT COALESCE(SUM(total), 0) AS total_sales, " +
                            "COALESCE(SUM(subtotal), 0) AS subtotal, " +
                            "COALESCE(SUM(tax), 0) AS tax_collected " +
                            "FROM job_orders " +
                            "WHERE payment_status = 'PAID' AND EXTRACT(YEAR FROM invoice_date) = ? AND EXTRACT(MONTH FROM invoice_date) = ?";
            try (PreparedStatement stmt = conn.prepareStatement(invSql)) {
                stmt.setInt(1, year);
                stmt.setInt(2, month);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        rep.setTotalSales(rs.getBigDecimal("total_sales"));
                        rep.setTotalTaxCollected(rs.getBigDecimal("tax_collected"));
                    }
                }
            }

            // 3. Labor vs Parts breakdown for paid jobs
            String laborSql = "SELECT COALESCE(SUM(ji.quantity * ji.unit_price), 0) AS labor_total " +
                              "FROM job_items ji " +
                              "JOIN job_orders j ON ji.job_id = j.job_id " +
                              "WHERE ji.item_type = 'SERVICE' AND j.payment_status = 'PAID' AND EXTRACT(YEAR FROM j.invoice_date) = ? AND EXTRACT(MONTH FROM j.invoice_date) = ?";
            try (PreparedStatement stmt = conn.prepareStatement(laborSql)) {
                stmt.setInt(1, year);
                stmt.setInt(2, month);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        rep.setTotalLaborRevenue(rs.getBigDecimal("labor_total"));
                    }
                }
            }

            String partSql = "SELECT COALESCE(SUM(ji.quantity * ji.unit_price), 0) AS parts_total " +
                             "FROM job_items ji " +
                             "JOIN job_orders j ON ji.job_id = j.job_id " +
                             "WHERE ji.item_type = 'PART' AND j.payment_status = 'PAID' AND EXTRACT(YEAR FROM j.invoice_date) = ? AND EXTRACT(MONTH FROM j.invoice_date) = ?";
            try (PreparedStatement stmt = conn.prepareStatement(partSql)) {
                stmt.setInt(1, year);
                stmt.setInt(2, month);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        rep.setTotalPartsRevenue(rs.getBigDecimal("parts_total"));
                    }
                }
            }

            // 4. New customer count
            String custSql = "SELECT COUNT(*) FROM customers WHERE EXTRACT(YEAR FROM created_at) = ? AND EXTRACT(MONTH FROM created_at) = ?";
            try (PreparedStatement stmt = conn.prepareStatement(custSql)) {
                stmt.setInt(1, year);
                stmt.setInt(2, month);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        rep.setNewCustomersRegistered(rs.getInt(1));
                    }
                }
            }

            // 5. Mechanic Performance breakdown
            String mechSql = "SELECT s.full_name AS mechanic_name, COUNT(j.job_id) AS total_jobs, " +
                             "COUNT(CASE WHEN j.status IN ('COMPLETED', 'INVOICED') THEN 1 END) AS completed_jobs " +
                             "FROM staff s " +
                             "LEFT JOIN job_orders j ON s.staff_id = j.mechanic_id " +
                             "WHERE s.role = 'MECHANIC' AND s.active = TRUE " +
                             "GROUP BY s.staff_id, s.full_name " +
                             "ORDER BY completed_jobs DESC";
            List<java.util.Map<String, Object>> mechList = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(mechSql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("name", rs.getString("mechanic_name"));
                    map.put("totalJobs", rs.getInt("total_jobs"));
                    map.put("completedJobs", rs.getInt("completed_jobs"));
                    mechList.add(map);
                }
            }
            rep.setMechanicPerformance(mechList);

            // 6. Popular Services
            String popSql = "SELECT c.name, COUNT(ji.job_item_id) AS usage_count, SUM(ji.quantity * ji.unit_price) AS total_charge " +
                            "FROM job_items ji " +
                            "JOIN catalog_items c ON ji.item_id = c.item_id " +
                            "WHERE ji.item_type = 'SERVICE' " +
                            "GROUP BY c.item_id, c.name " +
                            "ORDER BY usage_count DESC LIMIT 5";
            List<java.util.Map<String, Object>> popList = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(popSql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("name", rs.getString("name"));
                    map.put("usageCount", rs.getInt("usage_count"));
                    map.put("totalCharge", rs.getBigDecimal("total_charge"));
                    popList.add(map);
                }
            }
            rep.setPopularServices(popList);

        } catch (SQLException e) {
            logger.error("Error generating monthly report for {}-{}", year, month, e);
        }
        return rep;
    }

    private Invoice mapRow(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setInvoiceId(rs.getLong("invoice_id"));
        inv.setJobId(rs.getLong("job_id"));
        if (rs.getDate("invoice_date") != null) inv.setInvoiceDate(rs.getDate("invoice_date").toLocalDate());
        inv.setSubtotal(rs.getBigDecimal("subtotal"));
        inv.setTax(rs.getBigDecimal("tax"));
        inv.setDiscount(rs.getBigDecimal("discount"));
        inv.setTotal(rs.getBigDecimal("total"));
        inv.setPaymentStatus(rs.getString("payment_status"));
        inv.setPaymentMethod(rs.getString("payment_method"));
        if (rs.getTimestamp("paid_at") != null) inv.setPaidAt(rs.getTimestamp("paid_at").toLocalDateTime());

        inv.setPlateNo(rs.getString("plate_no"));
        inv.setCustomerName(rs.getString("customer_name"));
        inv.setCustomerPhone(rs.getString("customer_phone"));
        return inv;
    }
}
