package com.jobportal.dao.impl;

import com.jobportal.config.DBConnection;
import com.jobportal.dao.MessageDAO;
import com.jobportal.model.Message;
import com.jobportal.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAOImpl implements MessageDAO {

    @Override
    public boolean sendMessage(Message message) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, content) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, message.getSenderId());
            stmt.setInt(2, message.getReceiverId());
            stmt.setString(3, message.getContent());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
        return false;
    }

    @Override
    public List<Message> getConversation(int user1Id, int user2Id) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, u1.full_name as sender_name, u2.full_name as receiver_name " +
                     "FROM messages m " +
                     "JOIN users u1 ON m.sender_id = u1.id " +
                     "JOIN users u2 ON m.receiver_id = u2.id " +
                     "WHERE (m.sender_id = ? AND m.receiver_id = ?) " +
                     "   OR (m.sender_id = ? AND m.receiver_id = ?) " +
                     "ORDER BY m.sent_at ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, user1Id);
            stmt.setInt(2, user2Id);
            stmt.setInt(3, user2Id);
            stmt.setInt(4, user1Id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Message msg = new Message();
                msg.setId(rs.getInt("id"));
                msg.setSenderId(rs.getInt("sender_id"));
                msg.setReceiverId(rs.getInt("receiver_id"));
                msg.setContent(rs.getString("content"));
                msg.setRead(rs.getBoolean("is_read"));
                msg.setSentAt(rs.getTimestamp("sent_at"));
                msg.setSenderName(rs.getString("sender_name"));
                msg.setReceiverName(rs.getString("receiver_name"));
                messages.add(msg);
            }
        } catch (SQLException e) {
            System.err.println("Error getting conversation: " + e.getMessage());
        }
        return messages;
    }

    @Override
    public List<User> getChatContacts(int userId) {
        List<User> contacts = new ArrayList<>();
        String sql = "SELECT DISTINCT u.* FROM users u " +
                     "JOIN messages m ON (u.id = m.sender_id OR u.id = m.receiver_id) " +
                     "WHERE (m.sender_id = ? OR m.receiver_id = ?) AND u.id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setFullName(rs.getString("full_name"));
                u.setEmail(rs.getString("email"));
                u.setUserType(rs.getString("user_type"));
                contacts.add(u);
            }
        } catch (SQLException e) {
            System.err.println("Error getting chat contacts: " + e.getMessage());
        }
        return contacts;
    }

    @Override
    public int getUnreadCount(int userId) {
        String sql = "SELECT COUNT(*) FROM messages WHERE receiver_id = ? AND is_read = FALSE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error getting unread count: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public boolean markConversationAsRead(int senderId, int receiverId) {
        String sql = "UPDATE messages SET is_read = TRUE WHERE sender_id = ? AND receiver_id = ? AND is_read = FALSE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error marking messages read: " + e.getMessage());
        }
        return false;
    }
}
