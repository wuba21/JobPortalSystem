import com.jobportal.config.DBConnection;
import java.sql.Connection;
import java.sql.Statement;

public class CreateTableScript {
    public static void main(String[] args) {
        String sql = "CREATE TABLE IF NOT EXISTS contact_messages (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY," +
                     "name VARCHAR(100)," +
                     "email VARCHAR(100)," +
                     "subject VARCHAR(200)," +
                     "message TEXT," +
                     "status VARCHAR(20) DEFAULT 'Unread'," +
                     "sent_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                     ");";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
