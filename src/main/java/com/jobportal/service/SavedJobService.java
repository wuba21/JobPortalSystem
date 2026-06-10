package com.jobportal.service;

import com.jobportal.dao.SavedJobDAO;
import com.jobportal.dao.impl.SavedJobDAOImpl;
import com.jobportal.model.Job;

import java.util.List;

public class SavedJobService {

    private final SavedJobDAO savedJobDAO;

    public SavedJobService() {
        this.savedJobDAO = new SavedJobDAOImpl();
    }

    public boolean saveJob(int userId, int jobId) {
        if (savedJobDAO.isJobSaved(userId, jobId)) {
            return false;
        }
        return savedJobDAO.saveJob(userId, jobId);
    }

    public boolean removeSavedJob(int userId, int jobId) {
        return savedJobDAO.removeSavedJob(userId, jobId);
    }

    public boolean isJobSaved(int userId, int jobId) {
        return savedJobDAO.isJobSaved(userId, jobId);
    }

    public List<Job> getSavedJobs(int userId) {
        return savedJobDAO.getSavedJobs(userId);
    }
}
