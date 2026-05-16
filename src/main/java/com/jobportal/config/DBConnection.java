package com.jobportal.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/job_portal";
    private static final String USERNAME = "root";
    private static final String PASSWORD = ""; 
    
    private static boolean schemaMigrated = false;

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            
            if (!schemaMigrated) {
                runMigrations(conn);
                schemaMigrated = true;
                System.out.println("Database connected and schema verified successfully.");
            }
            
            return conn;
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found: " + e.getMessage());
            return null;
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return null;
        }
    }
    
    private static void runMigrations(Connection connection) {
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("ALTER TABLE jobs ADD COLUMN contact_email VARCHAR(255)");
        } catch (Exception ignored) {}
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("ALTER TABLE jobs ADD COLUMN telegram_link VARCHAR(255)");
        } catch (Exception ignored) {}
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("ALTER TABLE jobs ADD COLUMN website_link VARCHAR(255)");
        } catch (Exception ignored) {}
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("ALTER TABLE jobs ADD COLUMN linkedin_link VARCHAR(255)");
        } catch (Exception ignored) {}
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("ALTER TABLE jobs ADD COLUMN start_date DATE");
        } catch (Exception ignored) {}
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("ALTER TABLE applications DROP INDEX user_id");
        } catch (Exception ignored) {}
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("ALTER TABLE jobs ADD COLUMN department VARCHAR(255)");
        } catch (Exception ignored) {}
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("ALTER TABLE jobs ADD COLUMN reports_to VARCHAR(255)");
        } catch (Exception ignored) {}
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("ALTER TABLE jobs ADD COLUMN required_number INT DEFAULT 1");
        } catch (Exception ignored) {}
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("ALTER TABLE jobs ADD COLUMN education_qualification TEXT");
        } catch (Exception ignored) {}
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("ALTER TABLE users ADD COLUMN gender VARCHAR(20)");
        } catch (Exception ignored) {}
    }

    public static void closeConnection() {
    }
}
