package com.jobportal.model;

import java.sql.Timestamp;

public class Application {
    private int id;
    private int jobId;
    private int userId;
    private String coverLetter;
    private String resumePath;
    private String status; // PENDING, REVIEWED, SHORTLISTED, REJECTED, ACCEPTED
    private Timestamp appliedAt;
    private Timestamp updatedAt;

    // For display purposes
    private String jobTitle;
    private String applicantName;
    private String companyName;

    public Application() {}

    public Application(int jobId, int userId, String coverLetter) {
        this.jobId = jobId;
        this.userId = userId;
        this.coverLetter = coverLetter;
        this.status = "PENDING";
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getJobId() { return jobId; }
    public void setJobId(int jobId) { this.jobId = jobId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }

    public String getResumePath() { return resumePath; }
    public void setResumePath(String resumePath) { this.resumePath = resumePath; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getAppliedAt() { return appliedAt; }
    public void setAppliedAt(Timestamp appliedAt) { this.appliedAt = appliedAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    @Override
    public String toString() {
        return "Application{id=" + id + ", jobId=" + jobId + ", userId=" + userId + ", status='" + status + "'}";
    }
}
