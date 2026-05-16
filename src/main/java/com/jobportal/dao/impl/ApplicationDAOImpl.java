package com.jobportal.dao.impl;

import com.jobportal.config.DBConnection;
import com.jobportal.dao.ApplicationDAO;
import com.jobportal.model.Application;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAOImpl implements ApplicationDAO {

    @Override
    public boolean create(Application application) {
        String sql = "INSERT INTO applications (job_id, user_id, cover_letter, resume_path, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, application.getJobId());
            stmt.setInt(2, application.getUserId());
            stmt.setString(3, application.getCoverLetter());
            stmt.setString(4, application.getResumePath());
            stmt.setString(5, application.getStatus() != null ? application.getStatus() : "PENDING");
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) application.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Create application error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public Application findById(int id) {
        String sql = "SELECT a.*, j.title AS job_title, u.full_name "
                + "AS applicant_name FROM applications a JOIN jobs j ON a.job_id ="
                + " j.id JOIN users u ON a.user_id = u.id WHERE a.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapResultSetToApplication(rs);
        } catch (SQLException e) {
            System.err.println("Find application error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Application> findByUserId(int userId) {
        List<Application> applications = new ArrayList<>();
        String sql = "SELECT a.*, j.title AS job_title, u.full_name AS applicant_name,"
                + " e.company_name FROM applications a JOIN jobs j ON a.job_id = j.id JOIN users u ON a.user_id = u.id JOIN employers emp ON j.employer_id ="
                + " emp.id JOIN users e_u ON emp.user_id ="
                + " e_u.id LEFT JOIN employers e ON j.employer_id = e.id WHERE a.user_id = ? ORDER BY a.applied_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) applications.add(mapResultSetToApplication(rs));
        } catch (SQLException e) {
            System.err.println("Find applications by user error: " + e.getMessage());
        }
        return applications;
    }

    @Override
    public List<Application> findByJobId(int jobId) {
        List<Application> applications = new ArrayList<>();
        String sql = "SELECT a.*, j.title AS job_title, u.full_name AS applicant_name FROM applications a JOIN jobs j ON a.job_id = j.id JOIN users u ON a.user_id = u.id WHERE a.job_id = ? ORDER BY a.applied_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, jobId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) applications.add(mapResultSetToApplication(rs));
        } catch (SQLException e) {
            System.err.println("Find applications by job error: " + e.getMessage());
        }
        return applications;
    }

    @Override
    public List<Application> findByEmployerId(int employerId) {
        List<Application> applications = new ArrayList<>();
        String sql = "SELECT a.*, j.title AS job_title, u.full_name AS applicant_name FROM applications a JOIN jobs j ON a.job_id = j.id JOIN users u ON a.user_id = u.id WHERE j.employer_id = ? ORDER BY a.applied_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, employerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) applications.add(mapResultSetToApplication(rs));
        } catch (SQLException e) {
            System.err.println("Find applications by employer error: " + e.getMessage());
        }
        return applications;
    }

    @Override
    public List<Application> findAll() {
        List<Application> applications = new ArrayList<>();
        String sql = "SELECT a.*, j.title AS job_title, u.full_name AS applicant_name FROM applications a JOIN jobs j ON a.job_id = j.id JOIN users u ON a.user_id = u.id ORDER BY a.applied_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) applications.add(mapResultSetToApplication(rs));
        } catch (SQLException e) {
            System.err.println("Find all applications error: " + e.getMessage());
        }
        return applications;
    }

    @Override
    public boolean updateStatus(int applicationId, String status) {
        String sql = "UPDATE applications SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, applicationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Update application status error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM applications WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Delete application error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM applications";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Count applications error: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM applications WHERE status = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Count applications by status error: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public boolean hasApplied(int userId, int jobId) {
        String sql = "SELECT COUNT(*) FROM applications WHERE user_id = ? AND job_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, jobId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Has applied error: " + e.getMessage());
        }
        return false;
    }

    private Application mapResultSetToApplication(ResultSet rs) throws SQLException {
        Application app = new Application();
        app.setId(rs.getInt("id"));
        app.setJobId(rs.getInt("job_id"));
        app.setUserId(rs.getInt("user_id"));
        app.setCoverLetter(rs.getString("cover_letter"));
        app.setResumePath(rs.getString("resume_path"));
        app.setStatus(rs.getString("status"));
        app.setAppliedAt(rs.getTimestamp("applied_at"));
        app.setUpdatedAt(rs.getTimestamp("updated_at"));
        try { app.setJobTitle(rs.getString("job_title")); } catch (SQLException ignored) {}
        try { app.setApplicantName(rs.getString("applicant_name")); } catch (SQLException ignored) {}
        try { app.setCompanyName(rs.getString("company_name")); } catch (SQLException ignored) {}
        return app;
    }
}
