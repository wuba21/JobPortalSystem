package com.jobportal.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class Job {
    private int id;
    private int employerId;
    private String title;
    private String description;
    private String requirements;
    private String location;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String jobType;
    private String category;
    private String experienceLevel;
    private boolean isActive;
    private Date startDate;
    private Date deadline;
    private Timestamp postedAt;
    private Timestamp updatedAt;
    private String department;
    private String reportsTo;
    
    // New fields
    private int requiredNumber = 1;
    private String educationQualification;
    private String shift;


    // Contact and Social Links
    private String contactEmail;
    private String telegramLink;
    private String websiteLink;
    private String linkedinLink;

    // For display purposes
    private String companyName;

    public Job() {}

    public Job(int employerId, String title, String description, String location, String jobType) {
        this.employerId = employerId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.jobType = jobType;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmployerId() { return employerId; }
    public void setEmployerId(int employerId) { this.employerId = employerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public BigDecimal getSalaryMin() { return salaryMin; }
    public void setSalaryMin(BigDecimal salaryMin) { this.salaryMin = salaryMin; }

    public BigDecimal getSalaryMax() { return salaryMax; }
    public void setSalaryMax(BigDecimal salaryMax) { this.salaryMax = salaryMax; }

    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getDeadline() { return deadline; }
    public void setDeadline(Date deadline) { this.deadline = deadline; }

    public Timestamp getPostedAt() { return postedAt; }
    public void setPostedAt(Timestamp postedAt) { this.postedAt = postedAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getReportsTo() { return reportsTo; }
    public void setReportsTo(String reportsTo) { this.reportsTo = reportsTo; }

    public int getRequiredNumber() { return requiredNumber; }
    public void setRequiredNumber(int requiredNumber) { this.requiredNumber = requiredNumber; }

    public String getEducationQualification() { return educationQualification; }
    public void setEducationQualification(String educationQualification) { this.educationQualification = educationQualification; }

    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }


    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getTelegramLink() { return telegramLink; }
    public void setTelegramLink(String telegramLink) { this.telegramLink = telegramLink; }

    public String getWebsiteLink() { return websiteLink; }
    public void setWebsiteLink(String websiteLink) { this.websiteLink = websiteLink; }

    public String getLinkedinLink() { return linkedinLink; }
    public void setLinkedinLink(String linkedinLink) { this.linkedinLink = linkedinLink; }

    @Override
    public String toString() {
        return "Job{id=" + id + ", title='" + title + "', location='" + location + "', jobType='" + jobType + "'}";
    }
}
