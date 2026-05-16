package com.jobportal.dao.impl;

import com.jobportal.config.DBConnection;
import com.jobportal.dao.JobDAO;
import com.jobportal.model.Job;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JobDAOImpl implements JobDAO {

    @Override
    public boolean create(Job job) {
        String sql = "INSERT INTO jobs (employer_id, title, description, requirements, location, salary_min, salary_max, job_type, category, experience_level, start_date, deadline, contact_email, telegram_link, website_link, linkedin_link, department, reports_to, required_number, education_qualification) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, job.getEmployerId());
            stmt.setString(2, job.getTitle());
            stmt.setString(3, job.getDescription());
            stmt.setString(4, job.getRequirements());
            stmt.setString(5, job.getLocation());
            stmt.setBigDecimal(6, job.getSalaryMin());
            stmt.setBigDecimal(7, job.getSalaryMax());
            stmt.setString(8, job.getJobType());
            stmt.setString(9, job.getCategory());
            stmt.setString(10, job.getExperienceLevel());
            stmt.setDate(11, job.getStartDate());
            stmt.setDate(12, job.getDeadline());
            stmt.setString(13, job.getContactEmail());
            stmt.setString(14, job.getTelegramLink());
            stmt.setString(15, job.getWebsiteLink());
            stmt.setString(16, job.getLinkedinLink());
            stmt.setString(17, job.getDepartment());
            stmt.setString(18, job.getReportsTo());
            stmt.setInt(19, job.getRequiredNumber());
            stmt.setString(20, job.getEducationQualification());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) job.setId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Create job error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public Job findById(int id) {
        String sql = "SELECT j.*, e.company_name FROM jobs j JOIN employers e ON j.employer_id = e.id WHERE j.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapResultSetToJob(rs);
        } catch (SQLException e) {
            System.err.println("Find job error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Job> findAll() {
        List<Job> jobs = new ArrayList<>();
        String sql = "SELECT j.*, e.company_name FROM jobs j JOIN employers e ON j.employer_id = e.id WHERE j.is_active = TRUE AND (j.deadline IS NULL OR j.deadline >= CURDATE()) ORDER BY j.posted_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) jobs.add(mapResultSetToJob(rs));
        } catch (SQLException e) {
            System.err.println("Find all jobs error: " + e.getMessage());
        }
        return jobs;
    }

    @Override
    public List<Job> findByEmployerId(int employerId) {
        List<Job> jobs = new ArrayList<>();
        String sql = "SELECT j.*, e.company_name FROM jobs j JOIN employers e ON j.employer_id = e.id WHERE j.employer_id = ? AND j.is_active = TRUE AND (j.deadline IS NULL OR j.deadline >= CURDATE()) ORDER BY j.posted_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, employerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) jobs.add(mapResultSetToJob(rs));
        } catch (SQLException e) {
            System.err.println("Find jobs by employer error: " + e.getMessage());
        }
        return jobs;
    }

    @Override
    public List<Job> search(String keyword, String location, String jobType) {
        List<Job> jobs = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT j.*, e.company_name FROM jobs j JOIN employers e ON j.employer_id = e.id WHERE j.is_active = TRUE AND (j.deadline IS NULL OR j.deadline >= CURDATE())");
        List<String> params = new ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (j.title LIKE ? OR j.description LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        if (location != null && !location.isEmpty()) {
            sql.append(" AND j.location LIKE ?");
            params.add("%" + location + "%");
        }
        if (jobType != null && !jobType.isEmpty()) {
            sql.append(" AND j.job_type = ?");
            params.add(jobType);
        }
        sql.append(" ORDER BY j.posted_at DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i + 1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) jobs.add(mapResultSetToJob(rs));
        } catch (SQLException e) {
            System.err.println("Search jobs error: " + e.getMessage());
        }
        return jobs;
    }

    @Override
    public List<Job> findRecentJobs() {
        List<Job> jobs = new ArrayList<>();
        // jobs posted within last 5 days
        String sql = "SELECT j.*, e.company_name FROM jobs j JOIN employers e ON j.employer_id = e.id WHERE DATEDIFF(NOW(), j.posted_at) <= 5 AND j.is_active = TRUE AND (j.deadline IS NULL OR j.deadline >= CURDATE()) ORDER BY j.posted_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) jobs.add(mapResultSetToJob(rs));
        } catch (SQLException e) {
            System.err.println("Find recent jobs error: " + e.getMessage());
        }
        return jobs;
    }

    @Override
    public List<Job> findLongTermJobs() {
        List<Job> jobs = new ArrayList<>();
        // jobs with deadline > 30 days (long term)
        String sql = "SELECT j.*, e.company_name FROM jobs j JOIN employers e ON j.employer_id = e.id WHERE j.deadline IS NOT NULL AND DATEDIFF(j.deadline, NOW()) > 30 AND j.is_active = TRUE AND j.deadline >= CURDATE() ORDER BY j.posted_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) jobs.add(mapResultSetToJob(rs));
        } catch (SQLException e) {
            System.err.println("Find long term jobs error: " + e.getMessage());
        }
        return jobs;
    }

    @Override
    public boolean update(Job job) {
        String sql = "UPDATE jobs SET title = ?, description = ?, requirements = ?, location = ?, salary_min = ?, salary_max = ?, job_type = ?, category = ?, experience_level = ?, is_active = ?, start_date = ?, deadline = ?, contact_email = ?, telegram_link = ?, website_link = ?, linkedin_link = ?, department = ?, reports_to = ?, required_number = ?, education_qualification = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, job.getTitle());
            stmt.setString(2, job.getDescription());
            stmt.setString(3, job.getRequirements());
            stmt.setString(4, job.getLocation());
            stmt.setBigDecimal(5, job.getSalaryMin());
            stmt.setBigDecimal(6, job.getSalaryMax());
            stmt.setString(7, job.getJobType());
            stmt.setString(8, job.getCategory());
            stmt.setString(9, job.getExperienceLevel());
            stmt.setBoolean(10, job.isActive());
            stmt.setDate(11, job.getStartDate());
            stmt.setDate(12, job.getDeadline());
            stmt.setString(13, job.getContactEmail());
            stmt.setString(14, job.getTelegramLink());
            stmt.setString(15, job.getWebsiteLink());
            stmt.setString(16, job.getLinkedinLink());
            stmt.setString(17, job.getDepartment());
            stmt.setString(18, job.getReportsTo());
            stmt.setInt(19, job.getRequiredNumber());
            stmt.setString(20, job.getEducationQualification());
            stmt.setInt(21, job.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Update job error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "UPDATE jobs SET is_active = FALSE WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Delete job error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM jobs WHERE is_active = TRUE";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Count jobs error: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public int countByEmployer(int employerId) {
        String sql = "SELECT COUNT(*) FROM jobs WHERE employer_id = ? AND is_active = TRUE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, employerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Count employer jobs error: " + e.getMessage());
        }
        return 0;
    }

    private Job mapResultSetToJob(ResultSet rs) throws SQLException {
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
        try { job.setContactEmail(rs.getString("contact_email")); } catch (SQLException ignored) {}
        try { job.setTelegramLink(rs.getString("telegram_link")); } catch (SQLException ignored) {}
        try { job.setWebsiteLink(rs.getString("website_link")); } catch (SQLException ignored) {}
        try { job.setLinkedinLink(rs.getString("linkedin_link")); } catch (SQLException ignored) {}
        try { job.setCompanyName(rs.getString("company_name")); } catch (SQLException ignored) {}
        try { job.setDepartment(rs.getString("department")); } catch (SQLException ignored) {}
        try { job.setReportsTo(rs.getString("reports_to")); } catch (SQLException ignored) {}
        try { job.setRequiredNumber(rs.getInt("required_number")); } catch (SQLException ignored) {}
        try { job.setEducationQualification(rs.getString("education_qualification")); } catch (SQLException ignored) {}
        return job;
    }
}
