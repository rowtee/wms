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
            logger.info("WMS Web Application initialized successfully.");
        } catch (Exception e) {
            logger.error("Error during application context initialization", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Destroying WMS Web Application context...");
        DatabaseConfig.shutdown();
        logger.info("WMS Web Application shutdown complete.");
    }
}
