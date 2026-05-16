package com.jobportal.model;

import java.sql.Timestamp;

public class Employer {
    private int id;
    private int userId;
    private String companyName;
    private String industry;
    private String companyDescription;
    private String website;
    private String companySize;
    private boolean isVerified;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Employer() {}

    public Employer(int userId, String companyName, String industry) {
        this.userId = userId;
        this.companyName = companyName;
        this.industry = industry;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getCompanyDescription() { return companyDescription; }
    public void setCompanyDescription(String companyDescription) { this.companyDescription = companyDescription; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getCompanySize() { return companySize; }
    public void setCompanySize(String companySize) { this.companySize = companySize; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
