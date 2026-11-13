package com.wms.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Thread-safe singleton for managing database connections and HikariCP connection pooling
 * configured via {@code database.properties}.
 */
public final class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static final String CONFIG_FILE = "database.properties";

    private static volatile HikariDataSource dataSource;
    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }

    private DatabaseConfig() {
        // Prevent direct instantiation
    }

    private static void loadProperties() {
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                logger.error("Unable to find {} in classpath", CONFIG_FILE);
                throw new IllegalStateException("Database configuration file not found: " + CONFIG_FILE);
            }
            properties.load(input);
            logger.info("Database properties loaded successfully from {}", CONFIG_FILE);
        } catch (IOException e) {
            logger.error("Failed to load database properties", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Initializes the HikariCP DataSource.
     */
    public static synchronized void initialize() {
        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }

        try {
            HikariConfig config = new HikariConfig();
            config.setDriverClassName(getProperty("db.driver", "org.postgresql.Driver"));
            config.setJdbcUrl(getProperty("db.url", "jdbc:postgresql://localhost:5432/wms_db"));
            config.setUsername(getProperty("db.username", "postgres"));
            config.setPassword(getProperty("db.password", "postgres"));

            int maxPoolSize = Integer.parseInt(getProperty("db.pool.maximumPoolSize", "10"));
            int minIdle = Integer.parseInt(getProperty("db.pool.minimumIdle", "2"));
            long idleTimeout = Long.parseLong(getProperty("db.pool.idleTimeout", "30000"));
            long connectionTimeout = Long.parseLong(getProperty("db.pool.connectionTimeout", "20000"));
            long maxLifetime = Long.parseLong(getProperty("db.pool.maxLifetime", "1800000"));

            config.setMaximumPoolSize(maxPoolSize);
            config.setMinimumIdle(minIdle);
            config.setIdleTimeout(idleTimeout);
            config.setConnectionTimeout(connectionTimeout);
            config.setMaxLifetime(maxLifetime);
            config.setPoolName(getProperty("db.pool.poolName", "WmsHikariPool"));

            // Optimize Hikari connection validation
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);
            logger.info("HikariCP DataSource initialized successfully with URL: {}", config.getJdbcUrl());
        } catch (Exception e) {
            logger.error("Failed to initialize HikariCP DataSource", e);
            throw new RuntimeException("Could not initialize database connection pool", e);
        }
    }

    /**
     * Obtains a Connection from the pool.
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            initialize();
        }
        return dataSource.getConnection();
    }

    /**
     * Returns the underlying DataSource.
     */
    public static DataSource getDataSource() {
        if (dataSource == null || dataSource.isClosed()) {
            initialize();
        }
        return dataSource;
    }

    /**
     * Gets a loaded configuration property.
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Closes the connection pool on application shutdown.
     */
    public static synchronized void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Closing HikariCP DataSource...");
            dataSource.close();
            dataSource = null;
            logger.info("HikariCP DataSource closed.");
        }
    }
}
