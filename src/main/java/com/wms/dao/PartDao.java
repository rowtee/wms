package com.wms.dao;

import com.wms.config.DatabaseConfig;
import com.wms.model.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PartDao {
    private static final Logger logger = LoggerFactory.getLogger(PartDao.class);

    public List<Part> findAll() {
        String sql = "SELECT item_id AS part_id, item_code AS part_no, name, category, unit_price, cost_price, stock_qty, reorder_level, created_at " +
                     "FROM catalog_items WHERE item_type = 'PART' ORDER BY name ASC";
        List<Part> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Part(
                    rs.getLong("part_id"),
                    rs.getString("part_no"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getBigDecimal("unit_price"),
                    rs.getBigDecimal("cost_price"),
                    rs.getInt("stock_qty"),
                    rs.getInt("reorder_level"),
                    rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null
                ));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving parts", e);
        }
        return list;
    }

    public List<Part> findLowStock() {
        String sql = "SELECT item_id AS part_id, item_code AS part_no, name, category, unit_price, cost_price, stock_qty, reorder_level, created_at " +
                     "FROM catalog_items WHERE item_type = 'PART' AND stock_qty <= reorder_level ORDER BY stock_qty ASC";
        List<Part> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Part(
                    rs.getLong("part_id"),
                    rs.getString("part_no"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getBigDecimal("unit_price"),
                    rs.getBigDecimal("cost_price"),
                    rs.getInt("stock_qty"),
                    rs.getInt("reorder_level"),
                    rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null
                ));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving low stock parts", e);
        }
        return list;
    }

    public Optional<Part> findById(Long id) {
        String sql = "SELECT item_id AS part_id, item_code AS part_no, name, category, unit_price, cost_price, stock_qty, reorder_level, created_at " +
                     "FROM catalog_items WHERE item_id = ? AND item_type = 'PART'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Part(
                        rs.getLong("part_id"),
                        rs.getString("part_no"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getBigDecimal("unit_price"),
                        rs.getBigDecimal("cost_price"),
                        rs.getInt("stock_qty"),
                        rs.getInt("reorder_level"),
                        rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null
                    ));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding part by ID: {}", id, e);
        }
        return Optional.empty();
    }

    public boolean updateStock(Long partId, int quantityDelta) {
        String sql = "UPDATE catalog_items SET stock_qty = stock_qty + ? WHERE item_id = ? AND item_type = 'PART'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantityDelta);
            stmt.setLong(2, partId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating part stock for ID: {}", partId, e);
        }
        return false;
    }

    public Long create(Part p) {
        String sql = "INSERT INTO catalog_items (item_type, item_code, name, category, unit_price, cost_price, stock_qty, reorder_level) VALUES ('PART', ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, p.getPartNo().toUpperCase().trim());
            stmt.setString(2, p.getName());
            stmt.setString(3, p.getCategory());
            stmt.setBigDecimal(4, p.getUnitPrice());
            stmt.setBigDecimal(5, p.getCostPrice());
            stmt.setInt(6, p.getStockQty());
            stmt.setInt(7, p.getReorderLevel());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error creating part", e);
        }
        return null;
    }
}
