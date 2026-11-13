package com.wms.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application lifecycle listener that manages initialization and teardown
 * of application-wide resources such as the database connection pool.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(AppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Initializing WMS Web Application context...");
        try {
            DatabaseConfig.initialize();
            ensureSchemaInitialized();
            logger.info("WMS Web Application initialized successfully.");
        } catch (Exception e) {
            logger.error("Error during application context initialization", e);
        }
    }

    private void ensureSchemaInitialized() {
        try (java.sql.Connection conn = DatabaseConfig.getConnection()) {
            java.sql.DatabaseMetaData meta = conn.getMetaData();
            boolean tablesExist = false;
            try (java.sql.ResultSet rs = meta.getTables(null, "public", "customers", new String[]{"TABLE"})) {
                if (rs.next()) {
                    tablesExist = true;
                }
            }
            if (!tablesExist) {
                logger.info("Tables not found. Auto-initializing database schema from schema.sql...");
                try (java.io.InputStream in = getClass().getClassLoader().getResourceAsStream("schema.sql")) {
                    if (in != null) {
                        String sql = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                        try (java.sql.Statement stmt = conn.createStatement()) {
                            stmt.execute(sql);
                            logger.info("Database schema and seed data initialized successfully.");
                        }
                    } else {
                        logger.warn("schema.sql not found on classpath.");
                    }
                }
            } else {
                logger.info("Database schema already present.");
            }
        } catch (Exception e) {
            logger.warn("Schema initialization check warning: {}", e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Destroying WMS Web Application context...");
        DatabaseConfig.shutdown();
        logger.info("WMS Web Application shutdown complete.");
    }
}
