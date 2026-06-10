package com.jobportal.model;

import java.sql.Timestamp;

public class SavedJob {
    private int userId;
    private int jobId;
    private Timestamp savedAt;

    public SavedJob() {}

    public SavedJob(int userId, int jobId, Timestamp savedAt) {
        this.userId = userId;
        this.jobId = jobId;
        this.savedAt = savedAt;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getJobId() { return jobId; }
    public void setJobId(int jobId) { this.jobId = jobId; }

    public Timestamp getSavedAt() { return savedAt; }
    public void setSavedAt(Timestamp savedAt) { this.savedAt = savedAt; }
}
