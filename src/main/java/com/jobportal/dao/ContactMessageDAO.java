package com.jobportal.dao;

import com.jobportal.config.DBConnection;
import com.jobportal.model.ContactMessage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContactMessageDAO {

    public boolean create(ContactMessage msg) {
        String sql = "INSERT INTO contact_messages (name, email, subject, message, status) VALUES (?, ?, ?, ?, 'Unread')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, msg.getName());
            stmt.setString(2, msg.getEmail());
            stmt.setString(3, msg.getSubject());
            stmt.setString(4, msg.getMessage());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) msg.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<ContactMessage> findAll() {
        List<ContactMessage> list = new ArrayList<>();
        String sql = "SELECT * FROM contact_messages ORDER BY sent_date DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToMessage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countUnread() {
        String sql = "SELECT COUNT(*) FROM contact_messages WHERE status = 'Unread'";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean markAsRead(int id) {
        String sql = "UPDATE contact_messages SET status = 'Read' WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM contact_messages WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ContactMessage mapResultSetToMessage(ResultSet rs) throws SQLException {
        ContactMessage msg = new ContactMessage();
        msg.setId(rs.getInt("id"));
        msg.setName(rs.getString("name"));
        msg.setEmail(rs.getString("email"));
        msg.setSubject(rs.getString("subject"));
        msg.setMessage(rs.getString("message"));
        msg.setStatus(rs.getString("status"));
        msg.setSentDate(rs.getTimestamp("sent_date"));
        return msg;
    }
}
