package com.wms.dao;

import com.wms.config.DatabaseConfig;
import com.wms.model.ServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServiceTypeDao {
    private static final Logger logger = LoggerFactory.getLogger(ServiceTypeDao.class);

    public List<ServiceType> findAll() {
        String sql = "SELECT item_id AS service_type_id, name, unit_price AS labour_charge, interval_km, interval_months, description " +
                     "FROM catalog_items WHERE item_type = 'SERVICE' ORDER BY item_id ASC";
        List<ServiceType> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new ServiceType(
                    rs.getLong("service_type_id"),
                    rs.getString("name"),
                    rs.getBigDecimal("labour_charge"),
                    rs.getInt("interval_km"),
                    rs.getInt("interval_months"),
                    rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving service types", e);
        }
        return list;
    }
}
