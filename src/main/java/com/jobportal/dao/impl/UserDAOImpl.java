package com.jobportal.dao.impl;

import com.jobportal.config.DBConnection;
import com.jobportal.dao.UserDAO;
import com.jobportal.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    @Override
    public User authenticate(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND is_active = TRUE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                if (com.jobportal.util.PasswordUtil.checkPassword(password, user.getPassword())) {
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean register(User user) {
        String sql = "INSERT INTO users (full_name, email, password, phone, address, user_type, gender) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, com.jobportal.util.PasswordUtil.hashPassword(user.getPassword()));
            stmt.setString(4, user.getPhone());
            stmt.setString(5, user.getAddress());
            stmt.setString(6, user.getUserType());
            stmt.setString(7, user.getGender());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Registration error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Find user error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("Find user by email error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("Find all users error: " + e.getMessage());
        }
        return users;
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ?, address = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPhone());
            stmt.setString(4, user.getAddress());
            stmt.setInt(5, user.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Update user error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, com.jobportal.util.PasswordUtil.hashPassword(newPassword));
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Update password error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "UPDATE users SET is_active = FALSE WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Delete user error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public int countByType(String userType) {
        String sql = "SELECT COUNT(*) FROM users WHERE user_type = ? AND is_active = TRUE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userType);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Count users error: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public int getEmployerIdByUserId(int userId) {
        String sql = "SELECT id FROM employers WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Get employer ID error: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public int createEmployer(int userId, String companyName, String industry) {
        String sql = "INSERT INTO employers (user_id, company_name, industry) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.setString(2, companyName);
            stmt.setString(3, industry);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Create employer error: " + e.getMessage());
        }
        return -1;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setPhone(rs.getString("phone"));
        user.setAddress(rs.getString("address"));
        user.setUserType(rs.getString("user_type"));
        try { user.setGender(rs.getString("gender")); } catch (Exception ignored) {}
        user.setProfilePicture(rs.getString("profile_picture"));
        user.setActive(rs.getBoolean("is_active"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        return user;
    }
}
