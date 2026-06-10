package com.jobportal.dao.impl;

import com.jobportal.config.DBConnection;
import com.jobportal.dao.SavedJobDAO;
import com.jobportal.model.Job;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SavedJobDAOImpl implements SavedJobDAO {

    @Override
    public boolean saveJob(int userId, int jobId) {
        String sql = "INSERT INTO saved_jobs (user_id, job_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, jobId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Save job error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean removeSavedJob(int userId, int jobId) {
        String sql = "DELETE FROM saved_jobs WHERE user_id = ? AND job_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, jobId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Remove saved job error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean isJobSaved(int userId, int jobId) {
        String sql = "SELECT COUNT(*) FROM saved_jobs WHERE user_id = ? AND job_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, jobId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Check if job saved error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public List<Job> getSavedJobs(int userId) {
        List<Job> savedJobs = new ArrayList<>();
        String sql = "SELECT j.*, e.company_name FROM jobs j " +
                     "JOIN saved_jobs sj ON j.id = sj.job_id " +
                     "LEFT JOIN employers e ON j.employer_id = e.id " +
                     "WHERE sj.user_id = ? AND j.is_active = TRUE " +
                     "ORDER BY sj.saved_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Job job = new Job();
                    job.setId(rs.getInt("id"));
                    job.setEmployerId(rs.getInt("employer_id"));
                    job.setTitle(rs.getString("title"));
                    job.setDescription(rs.getString("description"));
                    job.setRequirements(rs.getString("requirements"));
                    job.setLocation(rs.getString("location"));
                    job.setSalaryMin(rs.getBigDecimal("salary_min"));
                    job.setSalaryMax(rs.getBigDecimal("salary_max"));
                    job.setJobType(rs.getString("job_type"));
                    job.setCategory(rs.getString("category"));
                    job.setExperienceLevel(rs.getString("experience_level"));
                    job.setActive(rs.getBoolean("is_active"));
                    job.setStartDate(rs.getDate("start_date"));
                    job.setDeadline(rs.getDate("deadline"));
                    job.setPostedAt(rs.getTimestamp("posted_at"));
                    job.setUpdatedAt(rs.getTimestamp("updated_at"));
                    
                    job.setContactEmail(rs.getString("contact_email"));
                    job.setTelegramLink(rs.getString("telegram_link"));
                    job.setWebsiteLink(rs.getString("website_link"));
                    job.setLinkedinLink(rs.getString("linkedin_link"));
                    
                    job.setDepartment(rs.getString("department"));
                    job.setReportsTo(rs.getString("reports_to"));
                    job.setRequiredNumber(rs.getInt("required_number"));
                    job.setEducationQualification(rs.getString("education_qualification"));
                    
                    try { job.setShift(rs.getString("shift")); } catch (Exception ignored) {}
                    
                    job.setCompanyName(rs.getString("company_name"));
                    savedJobs.add(job);
                }
            }
        } catch (SQLException e) {
            System.err.println("Get saved jobs error: " + e.getMessage());
        }
        return savedJobs;
    }
}
