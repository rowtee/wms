package com.wms.dao;

import com.wms.config.DatabaseConfig;
import com.wms.model.DashboardKpi;
import com.wms.model.JobOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JobOrderDao {
    private static final Logger logger = LoggerFactory.getLogger(JobOrderDao.class);

    private static final String SELECT_JOINED_SQL =
        "SELECT j.job_id, j.vehicle_id, j.advisor_id, j.mechanic_id, j.date_in, j.date_out, " +
        "j.mileage_in, j.complaint, j.diagnosis, j.status, j.next_service_date, j.next_service_km, j.created_at, " +
        "v.plate_no, CONCAT(v.manufacture_year, ' ', v.make, ' ', v.model) AS vehicle_info, " +
        "c.full_name AS customer_name, c.phone AS customer_phone, " +
        "sa.full_name AS advisor_name, sm.full_name AS mechanic_name " +
        "FROM job_orders j " +
        "JOIN vehicles v ON j.vehicle_id = v.vehicle_id " +
        "JOIN customers c ON v.customer_id = c.customer_id " +
        "JOIN staff sa ON j.advisor_id = sa.staff_id " +
        "LEFT JOIN staff sm ON j.mechanic_id = sm.staff_id ";

    public List<JobOrder> findAll(String statusFilter) {
        StringBuilder sql = new StringBuilder(SELECT_JOINED_SQL);
        boolean hasFilter = statusFilter != null && !statusFilter.trim().isEmpty() && !statusFilter.equalsIgnoreCase("ALL");
        if (hasFilter) {
            sql.append("WHERE j.status = ? ");
        }
        sql.append("ORDER BY j.job_id DESC");

        List<JobOrder> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            if (hasFilter) {
                stmt.setString(1, statusFilter.trim());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving job orders", e);
        }
        return list;
    }

    public List<JobOrder> findByMechanicId(Long mechanicId, String statusFilter) {
        StringBuilder sql = new StringBuilder(SELECT_JOINED_SQL);
        sql.append("WHERE j.mechanic_id = ? ");
        boolean hasFilter = statusFilter != null && !statusFilter.trim().isEmpty() && !statusFilter.equalsIgnoreCase("ALL");
        if (hasFilter) {
            sql.append("AND j.status = ? ");
        }
        sql.append("ORDER BY j.job_id DESC");

        List<JobOrder> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setLong(1, mechanicId);
            if (hasFilter) {
                stmt.setString(2, statusFilter.trim());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving job orders for mechanic: {}", mechanicId, e);
        }
        return list;
    }

    public Optional<JobOrder> findById(Long id) {
        String sql = SELECT_JOINED_SQL + "WHERE j.job_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding job order by ID: {}", id, e);
        }
        return Optional.empty();
    }

    public Long create(JobOrder job) {
        String sql = "INSERT INTO job_orders (vehicle_id, advisor_id, mechanic_id, mileage_in, complaint, diagnosis, status, next_service_date, next_service_km) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, job.getVehicleId());
            stmt.setLong(2, job.getAdvisorId());
            if (job.getMechanicId() != null) stmt.setLong(3, job.getMechanicId()); else stmt.setNull(3, Types.BIGINT);
            stmt.setInt(4, job.getMileageIn());
            stmt.setString(5, job.getComplaint());
            stmt.setString(6, job.getDiagnosis());
            stmt.setString(7, job.getStatus() != null ? job.getStatus() : "PENDING");
            if (job.getNextServiceDate() != null) stmt.setDate(8, Date.valueOf(job.getNextServiceDate())); else stmt.setNull(8, Types.DATE);
            if (job.getNextServiceKm() != null) stmt.setInt(9, job.getNextServiceKm()); else stmt.setNull(9, Types.INTEGER);

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error creating job order", e);
        }
        return null;
    }

    public boolean updateStatusAndDiagnosis(Long jobId, String status, String diagnosis) {
        String sql = "UPDATE job_orders SET status = ?, diagnosis = ?, date_out = CASE WHEN ? = 'COMPLETED' THEN CURRENT_TIMESTAMP ELSE date_out END WHERE job_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, diagnosis);
            stmt.setString(3, status);
            stmt.setLong(4, jobId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating status for job ID: {}", jobId, e);
        }
        return false;
    }

    public boolean updateMechanic(Long jobId, Long mechanicId) {
        String sql = "UPDATE job_orders SET mechanic_id = ? WHERE job_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (mechanicId != null && mechanicId > 0) {
                stmt.setLong(1, mechanicId);
            } else {
                stmt.setNull(1, Types.BIGINT);
            }
            stmt.setLong(2, jobId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error reassigning mechanic for job ID: {}", jobId, e);
        }
        return false;
    }

    public DashboardKpi getDashboardKpis() {
        DashboardKpi kpi = new DashboardKpi();
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Active Jobs
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM job_orders WHERE status IN ('PENDING', 'IN_PROGRESS', 'AWAITING_PARTS')")) {
                if (rs.next()) kpi.setActiveJobs(rs.getInt(1));
            }
            // Vehicles in shop
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(DISTINCT vehicle_id) FROM job_orders WHERE status IN ('PENDING', 'IN_PROGRESS', 'AWAITING_PARTS')")) {
                if (rs.next()) kpi.setVehiclesInShop(rs.getInt(1));
            }
            // Pending Invoices
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM job_orders WHERE payment_status = 'PENDING' AND (subtotal > 0 OR total > 0 OR status = 'INVOICED')")) {
                if (rs.next()) kpi.setPendingInvoices(rs.getInt(1));
            }
            // Low Stock Parts
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM catalog_items WHERE item_type = 'PART' AND stock_qty <= reorder_level")) {
                if (rs.next()) kpi.setLowStockParts(rs.getInt(1));
            }
            // Revenue
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COALESCE(SUM(total), 0) FROM job_orders WHERE payment_status = 'PAID'")) {
                if (rs.next()) kpi.setRevenueToday(rs.getBigDecimal(1));
            }
            // Completed jobs
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM job_orders WHERE status = 'COMPLETED'")) {
                if (rs.next()) kpi.setCompletedJobsThisMonth(rs.getInt(1));
            }
        } catch (SQLException e) {
            logger.error("Error fetching dashboard KPIs", e);
        }
        return kpi;
    }

    private JobOrder mapRow(ResultSet rs) throws SQLException {
        JobOrder j = new JobOrder();
        j.setJobId(rs.getLong("job_id"));
        j.setVehicleId(rs.getLong("vehicle_id"));
        j.setAdvisorId(rs.getLong("advisor_id"));
        long mechId = rs.getLong("mechanic_id");
        if (!rs.wasNull()) j.setMechanicId(mechId);
        if (rs.getTimestamp("date_in") != null) j.setDateIn(rs.getTimestamp("date_in").toLocalDateTime());
        if (rs.getTimestamp("date_out") != null) j.setDateOut(rs.getTimestamp("date_out").toLocalDateTime());
        j.setMileageIn(rs.getInt("mileage_in"));
        j.setComplaint(rs.getString("complaint"));
        j.setDiagnosis(rs.getString("diagnosis"));
        j.setStatus(rs.getString("status"));
        if (rs.getDate("next_service_date") != null) j.setNextServiceDate(rs.getDate("next_service_date").toLocalDate());
        int km = rs.getInt("next_service_km");
        if (!rs.wasNull()) j.setNextServiceKm(km);
        if (rs.getTimestamp("created_at") != null) j.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        j.setPlateNo(rs.getString("plate_no"));
        j.setVehicleInfo(rs.getString("vehicle_info"));
        j.setCustomerName(rs.getString("customer_name"));
        j.setCustomerPhone(rs.getString("customer_phone"));
        j.setAdvisorName(rs.getString("advisor_name"));
        j.setMechanicName(rs.getString("mechanic_name"));
        return j;
    }
}
