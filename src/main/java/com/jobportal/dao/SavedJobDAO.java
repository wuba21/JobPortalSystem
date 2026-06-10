package com.jobportal.dao;

import com.jobportal.model.Job;
import java.util.List;

public interface SavedJobDAO {
    boolean saveJob(int userId, int jobId);
    boolean removeSavedJob(int userId, int jobId);
    boolean isJobSaved(int userId, int jobId);
    List<Job> getSavedJobs(int userId);
}
