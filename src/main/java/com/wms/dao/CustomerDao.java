package com.wms.dao;

import com.wms.config.DatabaseConfig;
import com.wms.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDao {
    private static final Logger logger = LoggerFactory.getLogger(CustomerDao.class);

    public List<Customer> findAll() {
        String sql = "SELECT c.customer_id, c.full_name, c.phone, c.email, c.address, c.created_at, " +
                     "COUNT(v.vehicle_id) AS vehicle_count " +
                     "FROM customers c " +
                     "LEFT JOIN vehicles v ON c.customer_id = v.customer_id " +
                     "GROUP BY c.customer_id, c.full_name, c.phone, c.email, c.address, c.created_at " +
                     "ORDER BY c.customer_id DESC";
        List<Customer> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Customer c = new Customer(
                    rs.getLong("customer_id"),
                    rs.getString("full_name"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getString("address"),
                    rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null
                );
                c.setVehicleCount(rs.getInt("vehicle_count"));
                list.add(c);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving customers", e);
        }
        return list;
    }

    public Optional<Customer> findById(Long id) {
        String sql = "SELECT customer_id, full_name, phone, email, address, created_at FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Customer(
                        rs.getLong("customer_id"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null
                    ));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding customer by ID: {}", id, e);
        }
        return Optional.empty();
    }

    public List<Customer> searchByNameOrPhone(String keyword) {
        String sql = "SELECT c.customer_id, c.full_name, c.phone, c.email, c.address, c.created_at, " +
                     "COUNT(v.vehicle_id) AS vehicle_count " +
                     "FROM customers c " +
                     "LEFT JOIN vehicles v ON c.customer_id = v.customer_id " +
                     "WHERE LOWER(c.full_name) LIKE LOWER(?) OR c.phone LIKE ? " +
                     "GROUP BY c.customer_id, c.full_name, c.phone, c.email, c.address, c.created_at " +
                     "ORDER BY c.full_name ASC";
        List<Customer> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + (keyword != null ? keyword.trim() : "") + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Customer c = new Customer(
                        rs.getLong("customer_id"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null
                    );
                    c.setVehicleCount(rs.getInt("vehicle_count"));
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching customers by name or phone: {}", keyword, e);
        }
        return list;
    }

    public Long create(Customer c) {
        String sql = "INSERT INTO customers (full_name, phone, email, address) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, c.getFullName());
            stmt.setString(2, c.getPhone());
            stmt.setString(3, c.getEmail());
            stmt.setString(4, c.getAddress());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error creating customer", e);
        }
        return null;
    }
}
