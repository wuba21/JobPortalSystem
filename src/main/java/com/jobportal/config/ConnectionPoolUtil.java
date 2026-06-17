package com.jobportal.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * HikariCP Connection Pool Manager for Phase 1 Performance Enhancement.
 * Manages a pool of database connections for efficient, concurrent access.
 * Reduces connection overhead and improves application performance.
 */
public class ConnectionPoolUtil {

    private static HikariDataSource dataSource;
    private static final Object LOCK = new Object();

    private static final String URL = "jdbc:mysql://localhost:3306/job_portal";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    private static final int MAX_POOL_SIZE = 20;
    private static final int MIN_IDLE_CONNECTIONS = 5;
    private static final long CONNECTION_TIMEOUT = 20000; // 20 seconds
    private static final long IDLE_TIMEOUT = 300000; // 5 minutes
    private static final long MAX_LIFETIME = 1800000; // 30 minutes

    /**
     * Initialize the connection pool (called once at application startup).
     */
    public static void initialize() {
        if (dataSource == null) {
            synchronized (LOCK) {
                if (dataSource == null) {
                    try {
                        HikariConfig config = new HikariConfig();
                        config.setJdbcUrl(URL);
                        config.setUsername(USERNAME);
                        config.setPassword(PASSWORD);
                        config.setMaximumPoolSize(MAX_POOL_SIZE);
                        config.setMinimumIdle(MIN_IDLE_CONNECTIONS);
                        config.setConnectionTimeout(CONNECTION_TIMEOUT);
                        config.setIdleTimeout(IDLE_TIMEOUT);
                        config.setMaxLifetime(MAX_LIFETIME);
                        config.setAutoCommit(true);
                        config.setLeakDetectionThreshold(60000); // 60 seconds
                        config.setPoolName("JobPortalPool");

                        dataSource = new HikariDataSource(config);
                        System.out.println("✅ HikariCP Connection Pool initialized successfully.");
                        System.out.println("   Max Pool Size: " + MAX_POOL_SIZE);
                        System.out.println("   Min Idle Connections: " + MIN_IDLE_CONNECTIONS);
                    } catch (Exception e) {
                        System.err.println("❌ Failed to initialize connection pool: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Get a connection from the pool.
     * 
     * @return Database connection
     * @throws SQLException if connection cannot be obtained
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            initialize();
        }
        return dataSource.getConnection();
    }

    /**
     * Get pool statistics for monitoring.
     * 
     * @return String containing pool statistics
     */
    public static String getPoolStats() {
        if (dataSource == null) {
            return "Pool not initialized";
        }
        return String.format(
                "Active: %d | Idle: %d | Waiting: %d | Total: %d",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection(),
                dataSource.getHikariPoolMXBean().getTotalConnections());
    }

    /**
     * Close the connection pool (called at application shutdown).
     */
    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("✅ Connection pool closed successfully.");
        }
    }
}
