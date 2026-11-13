package com.wms.dao;

import com.wms.config.DatabaseConfig;
import com.wms.model.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for handling CRUD operations on Item records.
 */
public class ItemDao {

    private static final Logger logger = LoggerFactory.getLogger(ItemDao.class);

    private static final String SELECT_ALL_SQL = "SELECT id, sku, name, category, quantity, unit_price, created_at FROM items ORDER BY id ASC";
    private static final String SELECT_BY_ID_SQL = "SELECT id, sku, name, category, quantity, unit_price, created_at FROM items WHERE id = ?";
    private static final String COUNT_SQL = "SELECT COUNT(*) FROM items";

    public List<Item> findAll() {
        List<Item> items = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                items.add(mapRowToItem(rs));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving all items from database", e);
        }
        return items;
    }

    public Optional<Item> findById(Long id) {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_BY_ID_SQL)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToItem(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving item with ID: {}", id, e);
        }
        return Optional.empty();
    }

    public long count() {
        try (Connection connection = DatabaseConfig.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(COUNT_SQL)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting items in database", e);
        }
        return 0;
    }

    private Item mapRowToItem(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("created_at");
        LocalDateTime createdAt = (timestamp != null) ? timestamp.toLocalDateTime() : null;

        return new Item(
                rs.getLong("id"),
                rs.getString("sku"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getInt("quantity"),
                rs.getBigDecimal("unit_price"),
                createdAt
        );
    }
}
