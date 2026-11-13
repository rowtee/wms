package com.wms.dao;

import com.wms.config.DatabaseConfig;
import com.wms.model.JobServiceItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JobServiceDao {
    private static final Logger logger = LoggerFactory.getLogger(JobServiceDao.class);

    public List<JobServiceItem> findByJobId(Long jobId) {
        String sql = "SELECT ji.job_item_id AS job_service_id, ji.job_id, ji.item_id AS service_type_id, ji.unit_price AS labour_charge, ji.notes, " +
                     "c.name AS service_name " +
                     "FROM job_items ji " +
                     "JOIN catalog_items c ON ji.item_id = c.item_id " +
                     "WHERE ji.job_id = ? AND ji.item_type = 'SERVICE' ORDER BY ji.job_item_id ASC";
        List<JobServiceItem> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, jobId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new JobServiceItem(
                        rs.getLong("job_service_id"),
                        rs.getLong("job_id"),
                        rs.getLong("service_type_id"),
                        rs.getBigDecimal("labour_charge"),
                        rs.getString("notes"),
                        rs.getString("service_name")
                    ));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving job services for job ID: {}", jobId, e);
        }
        return list;
    }

    public boolean addServiceToJob(Long jobId, Long serviceTypeId, String notes) {
        String sql = "INSERT INTO job_items (job_id, item_type, item_id, quantity, unit_price, notes) " +
                     "SELECT ?, 'SERVICE', item_id, 1, unit_price, ? FROM catalog_items WHERE item_id = ? AND item_type = 'SERVICE'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, jobId);
            stmt.setString(2, notes != null ? notes : "");
            stmt.setLong(3, serviceTypeId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error adding service to job ID: {}", jobId, e);
        }
        return false;
    }
}
