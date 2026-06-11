package com.jobportal.dao.impl;

import com.jobportal.config.DBConnection;
import com.jobportal.dao.CVDAO;
import com.jobportal.model.CV;

import java.sql.*;

/**
 * MySQL implementation of CVDAO.
 */
public class CVDAOImpl implements CVDAO {

    @Override
    public int save(CV cv) {
        String sql = "INSERT INTO applicant_cv (user_id, fullname, email, phone, address, linkedin, gender, " +
                     "university_name, degree, department, graduation_year, experience, skills, activities, " +
                     "objective, photo_path, cv_template, pdf_path) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, cv.getUserId());
            stmt.setString(2, cv.getFullName());
            stmt.setString(3, cv.getEmail());
            stmt.setString(4, cv.getPhone());
            stmt.setString(5, cv.getAddress());
            stmt.setString(6, cv.getLinkedin());
            stmt.setString(7, cv.getGender());
            stmt.setString(8, cv.getUniversityName());
            stmt.setString(9, cv.getDegree());
            stmt.setString(10, cv.getDepartment());
            stmt.setString(11, cv.getGraduationYear());
            stmt.setString(12, cv.getExperience());
            stmt.setString(13, cv.getSkills());
            stmt.setString(14, cv.getActivities());
            stmt.setString(15, cv.getObjective());
            stmt.setString(16, cv.getPhotoPath());
            stmt.setInt(17, cv.getCvTemplate());
            stmt.setString(18, cv.getPdfPath());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("CV save error: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public boolean update(CV cv) {
        String sql = "UPDATE applicant_cv SET fullname=?, email=?, phone=?, address=?, linkedin=?, gender=?, " +
                     "university_name=?, degree=?, department=?, graduation_year=?, experience=?, skills=?, " +
                     "activities=?, objective=?, photo_path=?, cv_template=?, pdf_path=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cv.getFullName());
            stmt.setString(2, cv.getEmail());
            stmt.setString(3, cv.getPhone());
            stmt.setString(4, cv.getAddress());
            stmt.setString(5, cv.getLinkedin());
            stmt.setString(6, cv.getGender());
            stmt.setString(7, cv.getUniversityName());
            stmt.setString(8, cv.getDegree());
            stmt.setString(9, cv.getDepartment());
            stmt.setString(10, cv.getGraduationYear());
            stmt.setString(11, cv.getExperience());
            stmt.setString(12, cv.getSkills());
            stmt.setString(13, cv.getActivities());
            stmt.setString(14, cv.getObjective());
            stmt.setString(15, cv.getPhotoPath());
            stmt.setInt(16, cv.getCvTemplate());
            stmt.setString(17, cv.getPdfPath());
            stmt.setInt(18, cv.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("CV update error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public CV findByUserId(int userId) {
        String sql = "SELECT * FROM applicant_cv WHERE user_id = ? ORDER BY created_at DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                CV cv = new CV();
                cv.setId(rs.getInt("id"));
                cv.setUserId(rs.getInt("user_id"));
                cv.setFullName(rs.getString("fullname"));
                cv.setEmail(rs.getString("email"));
                cv.setPhone(rs.getString("phone"));
                cv.setAddress(rs.getString("address"));
                cv.setLinkedin(rs.getString("linkedin"));
                cv.setGender(rs.getString("gender"));
                cv.setUniversityName(rs.getString("university_name"));
                cv.setDegree(rs.getString("degree"));
                cv.setDepartment(rs.getString("department"));
                cv.setGraduationYear(rs.getString("graduation_year"));
                cv.setExperience(rs.getString("experience"));
                cv.setSkills(rs.getString("skills"));
                cv.setActivities(rs.getString("activities"));
                cv.setObjective(rs.getString("objective"));
                cv.setPhotoPath(rs.getString("photo_path"));
                cv.setCvTemplate(rs.getInt("cv_template"));
                cv.setPdfPath(rs.getString("pdf_path"));
                cv.setCreatedAt(rs.getTimestamp("created_at"));
                return cv;
            }
        } catch (SQLException e) {
            System.err.println("CV findByUserId error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean updatePdfPath(int cvId, String pdfPath) {
        String sql = "UPDATE applicant_cv SET pdf_path = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pdfPath);
            stmt.setInt(2, cvId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("CV updatePdfPath error: " + e.getMessage());
        }
        return false;
    }
}
