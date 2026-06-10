package com.jobportal.dao.impl;

import com.jobportal.config.DBConnection;
import com.jobportal.dao.NotificationDAO;
import com.jobportal.model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAOImpl implements NotificationDAO {

    @Override
    public boolean createNotification(int userId, String message) {
        String sql = "INSERT INTO notifications (user_id, message) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, message);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Create notification error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public List<Notification> getNotificationsByUser(int userId) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("message"),
                            rs.getBoolean("is_read"),
                            rs.getTimestamp("created_at")
                    );
                    notifications.add(n);
                }
            }
        } catch (SQLException e) {
            System.err.println("Get notifications error: " + e.getMessage());
        }
        return notifications;
    }

    @Override
    public boolean markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notificationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Mark notification as read error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean clearAllNotifications(int userId) {
        String sql = "DELETE FROM notifications WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Clear notifications error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public int countUnread(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Count unread notifications error: " + e.getMessage());
        }
        return 0;
    }
}
