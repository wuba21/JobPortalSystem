package com.jobportal.service;

import com.jobportal.dao.ApplicationDAO;
import com.jobportal.dao.impl.ApplicationDAOImpl;
import com.jobportal.exception.ValidationException;
import com.jobportal.model.Application;

import java.util.List;

public class ApplicationService {

    private final ApplicationDAO applicationDAO;

    public ApplicationService() {
        this.applicationDAO = new ApplicationDAOImpl();
    }

    public boolean apply(Application application) throws ValidationException {
        if (applicationDAO.hasApplied(application.getUserId(), application.getJobId())) {
            throw new ValidationException("You have already applied for this job.");
        }
        return applicationDAO.create(application);
    }

    public Application findById(int id) {
        return applicationDAO.findById(id);
    }

    public List<Application> findByUserId(int userId) {
        return applicationDAO.findByUserId(userId);
    }

    public List<Application> findByJobId(int jobId) {
        return applicationDAO.findByJobId(jobId);
    }

    public List<Application> findByEmployerId(int employerId) {
        return applicationDAO.findByEmployerId(employerId);
    }

    public boolean updateStatus(int applicationId, String status) {
        return applicationDAO.updateStatus(applicationId, status);
    }

    public boolean delete(int id) {
        return applicationDAO.delete(id);
    }

    public List<Application> findAll() {
        return applicationDAO.findAll();
    }

    public int countAll() {
        return applicationDAO.countAll();
    }

    public boolean hasApplied(int userId, int jobId) {
        return applicationDAO.hasApplied(userId, jobId);
    }
}
