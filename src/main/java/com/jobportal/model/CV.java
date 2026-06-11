package com.jobportal.model;

import java.sql.Timestamp;

/**
 * Model class representing an applicant's CV/Resume data.
 */
public class CV {

    private int id;
    private int userId;

    // Personal Info
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String linkedin;
    private String gender;

    // Education
    private String universityName;
    private String degree;
    private String department;
    private String graduationYear;

    // Experience (stored as formatted text)
    private String experience;

    // Skills (comma-separated)
    private String skills;

    // Activities
    private String activities;

    // Career objective
    private String objective;

    // Photo & Template
    private String photoPath;
    private int cvTemplate; // 1, 2, 3, or 4

    // Generated PDF path
    private String pdfPath;

    private Timestamp createdAt;

    public CV() {}

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getLinkedin() { return linkedin; }
    public void setLinkedin(String linkedin) { this.linkedin = linkedin; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getUniversityName() { return universityName; }
    public void setUniversityName(String universityName) { this.universityName = universityName; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getGraduationYear() { return graduationYear; }
    public void setGraduationYear(String graduationYear) { this.graduationYear = graduationYear; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getActivities() { return activities; }
    public void setActivities(String activities) { this.activities = activities; }

    public String getObjective() { return objective; }
    public void setObjective(String objective) { this.objective = objective; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public int getCvTemplate() { return cvTemplate; }
    public void setCvTemplate(int cvTemplate) { this.cvTemplate = cvTemplate; }

    public String getPdfPath() { return pdfPath; }
    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
