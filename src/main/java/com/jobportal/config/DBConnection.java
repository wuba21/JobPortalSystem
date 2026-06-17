package com.jobportal.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database Connection Manager with HikariCP Connection Pooling (Phase 1).
 * Previously used direct connections; now uses connection pool for better
 * performance.
 */
public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/job_portal";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    private static boolean schemaMigrated = false;

    static {
        // Initialize connection pool on class load
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            ConnectionPoolUtil.initialize();
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL Driver not found: " + e.getMessage());
        }
    }

    /**
     * Get a connection from the HikariCP pool.
     * 
     * @return Pooled database connection
     */
    public static Connection getConnection() {
        try {
            Connection conn = ConnectionPoolUtil.getConnection();

            if (!schemaMigrated) {
                runMigrations(conn);
                schemaMigrated = true;
                System.out.println("✅ Database connected and schema verified successfully.");
            }

            return conn;
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            return null;
        }
    }

    private static void runMigrations(Connection connection) {
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("ALTER TABLE jobs ADD COLUMN contact_email VARCHAR(255)");
        } catch (Exception ignored) {
        }
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("ALTER TABLE jobs ADD COLUMN telegram_link VARCHAR(255)");
        } catch (Exception ignored) {
        }
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("ALTER TABLE jobs ADD COLUMN website_link VARCHAR(255)");
        } catch (Exception ignored) {
        }
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("ALTER TABLE jobs ADD COLUMN linkedin_link VARCHAR(255)");
        } catch (Exception ignored) {
        }
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("ALTER TABLE jobs ADD COLUMN start_date DATE");
        } catch (Exception ignored) {
        }
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("ALTER TABLE applications DROP INDEX user_id");
        } catch (Exception ignored) {
        }
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("ALTER TABLE jobs ADD COLUMN department VARCHAR(255)");
        } catch (Exception ignored) {
        }
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("ALTER TABLE jobs ADD COLUMN reports_to VARCHAR(255)");
        } catch (Exception ignored) {
        }
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("ALTER TABLE jobs ADD COLUMN required_number INT DEFAULT 1");
        } catch (Exception ignored) {
        }
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("ALTER TABLE jobs ADD COLUMN education_qualification TEXT");
        } catch (Exception ignored) {
        }
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("ALTER TABLE users ADD COLUMN gender VARCHAR(20)");
        } catch (Exception ignored) {
        }
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS notifications (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "user_id INT NOT NULL," +
                    "message TEXT NOT NULL," +
                    "is_read BOOLEAN DEFAULT FALSE," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ")");
        } catch (Exception ignored) {
        }
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS saved_jobs (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "user_id INT NOT NULL," +
                    "job_id INT NOT NULL," +
                    "saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE," +
                    "UNIQUE KEY unique_saved_job (user_id, job_id)" +
                    ")");
        } catch (Exception ignored) {
        }
        // applicant_cv table for CV Builder
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS applicant_cv (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "user_id INT NOT NULL," +
                    "fullname VARCHAR(255)," +
                    "email VARCHAR(255)," +
                    "phone VARCHAR(50)," +
                    "address TEXT," +
                    "linkedin VARCHAR(255)," +
                    "gender VARCHAR(20)," +
                    "university_name VARCHAR(255)," +
                    "degree VARCHAR(255)," +
                    "department VARCHAR(255)," +
                    "graduation_year VARCHAR(10)," +
                    "experience TEXT," +
                    "skills TEXT," +
                    "activities TEXT," +
                    "objective TEXT," +
                    "photo_path VARCHAR(512)," +
                    "cv_template INT DEFAULT 1," +
                    "pdf_path VARCHAR(512)," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ")");
        } catch (Exception ignored) {
        }

        // Add HYBRID to job_type ENUM
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute(
                    "ALTER TABLE jobs MODIFY COLUMN job_type ENUM('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'REMOTE', 'HYBRID') NOT NULL DEFAULT 'FULL_TIME'");
        } catch (Exception ignored) {
        }

        // Create messages table
        try (java.sql.Statement s = connection.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS messages (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "sender_id INT NOT NULL," +
                    "receiver_id INT NOT NULL," +
                    "content TEXT NOT NULL," +
                    "is_read BOOLEAN DEFAULT FALSE," +
                    "sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ")");
        } catch (Exception ignored) {
        }
    }

    public static void closeConnection() {
        ConnectionPoolUtil.close();
    }
}
