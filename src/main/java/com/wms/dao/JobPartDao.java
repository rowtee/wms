package com.wms.dao;

import com.wms.config.DatabaseConfig;
import com.wms.model.JobPartItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JobPartDao {
    private static final Logger logger = LoggerFactory.getLogger(JobPartDao.class);

    public List<JobPartItem> findByJobId(Long jobId) {
        String sql = "SELECT ji.job_item_id AS job_part_id, ji.job_id, ji.item_id AS part_id, ji.quantity, ji.unit_price, " +
                     "c.item_code AS part_no, c.name AS part_name " +
                     "FROM job_items ji " +
                     "JOIN catalog_items c ON ji.item_id = c.item_id " +
                     "WHERE ji.job_id = ? AND ji.item_type = 'PART' ORDER BY ji.job_item_id ASC";
        List<JobPartItem> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, jobId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new JobPartItem(
                        rs.getLong("job_part_id"),
                        rs.getLong("job_id"),
                        rs.getLong("part_id"),
                        rs.getInt("quantity"),
                        rs.getBigDecimal("unit_price"),
                        rs.getString("part_no"),
                        rs.getString("part_name")
                    ));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving job parts for job ID: {}", jobId, e);
        }
        return list;
    }

    public boolean addPartToJob(Long jobId, Long partId, int quantity) {
        String insertSql = "INSERT INTO job_items (job_id, item_type, item_id, quantity, unit_price) " +
                           "SELECT ?, 'PART', item_id, ?, unit_price FROM catalog_items WHERE item_id = ? AND item_type = 'PART'";
        String updateStockSql = "UPDATE catalog_items SET stock_qty = stock_qty - ? WHERE item_id = ? AND item_type = 'PART'";

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                 PreparedStatement updateStmt = conn.prepareStatement(updateStockSql)) {

                insertStmt.setLong(1, jobId);
                insertStmt.setInt(2, quantity);
                insertStmt.setLong(3, partId);
                insertStmt.executeUpdate();

                updateStmt.setInt(1, quantity);
                updateStmt.setLong(2, partId);
                updateStmt.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Error adding part to job ID: {}", jobId, e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return false;
    }
}
