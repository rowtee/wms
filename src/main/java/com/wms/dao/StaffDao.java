package com.wms.dao;

import com.wms.config.DatabaseConfig;
import com.wms.model.Staff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.wms.util.PasswordUtil;

public class StaffDao {
    private static final Logger logger = LoggerFactory.getLogger(StaffDao.class);

    public Optional<Staff> authenticate(String username, String rawPassword) {
        String sql = "SELECT staff_id, username, password_hash, full_name, role, phone, active FROM staff WHERE username = ? AND active = TRUE";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username.trim().toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (PasswordUtil.checkPassword(rawPassword, storedHash)) {
                        long staffId = rs.getLong("staff_id");
                        // If password in DB is still unencrypted plain text, automatically encrypt and save BCrypt hash!
                        if (!PasswordUtil.isBcryptHash(storedHash)) {
                            logger.info("Migrating plaintext password to BCrypt hash for staff: {}", username);
                            updatePassword(staffId, PasswordUtil.hashPassword(rawPassword));
                        }
                        return Optional.of(new Staff(
                            staffId,
                            rs.getString("username"),
                            rs.getString("full_name"),
                            rs.getString("role"),
                            rs.getString("phone"),
                            rs.getBoolean("active")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error authenticating user: {}", username, e);
        }
        return Optional.empty();
    }

    public boolean updatePassword(Long staffId, String newPasswordHash) {
        String sql = "UPDATE staff SET password_hash = ? WHERE staff_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPasswordHash);
            stmt.setLong(2, staffId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating password hash for staff ID: {}", staffId, e);
            return false;
        }
    }

    public List<Staff> findAll() {
        String sql = "SELECT staff_id, username, full_name, role, phone, active FROM staff WHERE active = TRUE ORDER BY full_name ASC";
        List<Staff> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Staff(
                    rs.getLong("staff_id"),
                    rs.getString("username"),
                    rs.getString("full_name"),
                    rs.getString("role"),
                    rs.getString("phone"),
                    rs.getBoolean("active")
                ));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving staff members", e);
        }
        return list;
    }

    public List<Staff> findByRole(String role) {
        String sql = "SELECT staff_id, username, full_name, role, phone, active FROM staff WHERE role = ? AND active = TRUE ORDER BY full_name ASC";
        List<Staff> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Staff(
                        rs.getLong("staff_id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("role"),
                        rs.getString("phone"),
                        rs.getBoolean("active")
                    ));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving staff by role: {}", role, e);
        }
        return list;
    }

    public Optional<Staff> findById(Long id) {
        String sql = "SELECT staff_id, username, full_name, role, phone, active FROM staff WHERE staff_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Staff(
                        rs.getLong("staff_id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("role"),
                        rs.getString("phone"),
                        rs.getBoolean("active")
                    ));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding staff by ID: {}", id, e);
        }
        return Optional.empty();
    }

    public Optional<Staff> findMechanicWithLeastJobs() {
        // Round-robin / workload-balanced assignment based on least active cars in bay (PENDING, IN_PROGRESS, AWAITING_PARTS)
        String sql = "SELECT s.staff_id, s.username, s.full_name, s.role, s.phone, s.active, " +
                     "COUNT(j.job_id) AS active_job_count " +
                     "FROM staff s " +
                     "LEFT JOIN job_orders j ON s.staff_id = j.mechanic_id AND j.status IN ('PENDING', 'IN_PROGRESS', 'AWAITING_PARTS') " +
                     "WHERE s.role = 'MECHANIC' AND s.active = TRUE " +
                     "GROUP BY s.staff_id, s.username, s.full_name, s.role, s.phone, s.active " +
                     "ORDER BY active_job_count ASC, s.staff_id ASC LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return Optional.of(new Staff(
                    rs.getLong("staff_id"),
                    rs.getString("username"),
                    rs.getString("full_name"),
                    rs.getString("role"),
                    rs.getString("phone"),
                    rs.getBoolean("active")
                ));
            }
        } catch (SQLException e) {
            logger.error("Error finding mechanic with least jobs", e);
        }
        return Optional.empty();
    }

    public List<java.util.Map<String, Object>> getMechanicsWorkload() {
        String sql = "SELECT s.staff_id, s.full_name, COUNT(j.job_id) AS active_cars " +
                     "FROM staff s " +
                     "LEFT JOIN job_orders j ON s.staff_id = j.mechanic_id AND j.status IN ('PENDING', 'IN_PROGRESS', 'AWAITING_PARTS') " +
                     "WHERE s.role = 'MECHANIC' AND s.active = TRUE " +
                     "GROUP BY s.staff_id, s.full_name " +
                     "ORDER BY active_cars ASC, s.staff_id ASC";
        List<java.util.Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("staffId", rs.getLong("staff_id"));
                map.put("fullName", rs.getString("full_name"));
                map.put("activeCars", rs.getInt("active_cars"));
                list.add(map);
            }
        } catch (SQLException e) {
            logger.error("Error fetching mechanics workload", e);
        }
        return list;
    }
}
