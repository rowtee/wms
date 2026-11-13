package com.wms.dao;

import com.wms.config.DatabaseConfig;
import com.wms.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VehicleDao {
    private static final Logger logger = LoggerFactory.getLogger(VehicleDao.class);

    public List<Vehicle> findAll() {
        String sql = "SELECT v.vehicle_id, v.customer_id, v.plate_no, v.make, v.model, v.manufacture_year, v.current_mileage, v.created_at, " +
                     "c.full_name AS customer_name, c.phone AS customer_phone " +
                     "FROM vehicles v " +
                     "JOIN customers c ON v.customer_id = c.customer_id " +
                     "ORDER BY v.vehicle_id DESC";
        List<Vehicle> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Vehicle v = new Vehicle(
                    rs.getLong("vehicle_id"),
                    rs.getLong("customer_id"),
                    rs.getString("plate_no"),
                    rs.getString("make"),
                    rs.getString("model"),
                    rs.getInt("manufacture_year"),
                    rs.getInt("current_mileage"),
                    rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null
                );
                v.setCustomerName(rs.getString("customer_name"));
                v.setCustomerPhone(rs.getString("customer_phone"));
                list.add(v);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving vehicles", e);
        }
        return list;
    }

    public List<Vehicle> findByCustomerId(Long customerId) {
        String sql = "SELECT vehicle_id, customer_id, plate_no, make, model, manufacture_year, current_mileage, created_at " +
                     "FROM vehicles WHERE customer_id = ? ORDER BY vehicle_id DESC";
        List<Vehicle> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Vehicle(
                        rs.getLong("vehicle_id"),
                        rs.getLong("customer_id"),
                        rs.getString("plate_no"),
                        rs.getString("make"),
                        rs.getString("model"),
                        rs.getInt("manufacture_year"),
                        rs.getInt("current_mileage"),
                        rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null
                    ));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding vehicles for customer ID: {}", customerId, e);
        }
        return list;
    }

    public Optional<Vehicle> findById(Long id) {
        String sql = "SELECT v.vehicle_id, v.customer_id, v.plate_no, v.make, v.model, v.manufacture_year, v.current_mileage, v.created_at, " +
                     "c.full_name AS customer_name, c.phone AS customer_phone " +
                     "FROM vehicles v " +
                     "JOIN customers c ON v.customer_id = c.customer_id " +
                     "WHERE v.vehicle_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Vehicle v = new Vehicle(
                        rs.getLong("vehicle_id"),
                        rs.getLong("customer_id"),
                        rs.getString("plate_no"),
                        rs.getString("make"),
                        rs.getString("model"),
                        rs.getInt("manufacture_year"),
                        rs.getInt("current_mileage"),
                        rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null
                    );
                    v.setCustomerName(rs.getString("customer_name"));
                    v.setCustomerPhone(rs.getString("customer_phone"));
                    return Optional.of(v);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding vehicle by ID: {}", id, e);
        }
        return Optional.empty();
    }

    public Optional<Vehicle> findByPlateNo(String plateNo) {
        String sql = "SELECT v.vehicle_id, v.customer_id, v.plate_no, v.make, v.model, v.manufacture_year, v.current_mileage, v.created_at, " +
                     "c.full_name AS customer_name, c.phone AS customer_phone " +
                     "FROM vehicles v " +
                     "JOIN customers c ON v.customer_id = c.customer_id " +
                     "WHERE UPPER(TRIM(v.plate_no)) = UPPER(TRIM(?))";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, plateNo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Vehicle v = new Vehicle(
                        rs.getLong("vehicle_id"),
                        rs.getLong("customer_id"),
                        rs.getString("plate_no"),
                        rs.getString("make"),
                        rs.getString("model"),
                        rs.getInt("manufacture_year"),
                        rs.getInt("current_mileage"),
                        rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null
                    );
                    v.setCustomerName(rs.getString("customer_name"));
                    v.setCustomerPhone(rs.getString("customer_phone"));
                    return Optional.of(v);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding vehicle by plate no: {}", plateNo, e);
        }
        return Optional.empty();
    }

    public boolean updateMileage(Long vehicleId, int newMileage) {
        String sql = "UPDATE vehicles SET current_mileage = ? WHERE vehicle_id = ? AND current_mileage < ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newMileage);
            stmt.setLong(2, vehicleId);
            stmt.setInt(3, newMileage);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating vehicle mileage", e);
        }
        return false;
    }

    public Long create(Vehicle v) {
        String sql = "INSERT INTO vehicles (customer_id, plate_no, make, model, manufacture_year, current_mileage) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, v.getCustomerId());
            stmt.setString(2, v.getPlateNo().toUpperCase().trim());
            stmt.setString(3, v.getMake());
            stmt.setString(4, v.getModel());
            stmt.setInt(5, v.getYear());
            stmt.setInt(6, v.getCurrentMileage() != null ? v.getCurrentMileage() : 0);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error creating vehicle", e);
        }
        return null;
    }
}
