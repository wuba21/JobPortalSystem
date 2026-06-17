package com.jobportal.service;

import com.jobportal.dao.JobDAO;
import com.jobportal.dao.impl.JobDAOImpl;
import com.jobportal.exception.ValidationException;
import com.jobportal.model.Job;

import java.util.List;

public class JobService {

    private final JobDAO jobDAO;

    public JobService() {
        this.jobDAO = new JobDAOImpl();
    }

    public boolean createJob(Job job) throws ValidationException {
        if (job.getTitle() == null || job.getTitle().trim().isEmpty()) {
            throw new ValidationException("Job title is required.");
        }
        if (job.getDescription() == null || job.getDescription().trim().isEmpty()) {
            throw new ValidationException("Job description is required.");
        }
        if (job.getLocation() == null || job.getLocation().trim().isEmpty()) {
            throw new ValidationException("Job location is required.");
        }
        return jobDAO.create(job);
    }

    public Job findById(int id) {
        return jobDAO.findById(id);
    }

    public List<Job> findAll() {
        return jobDAO.findAll();
    }

    public List<Job> findByEmployerId(int employerId) {
        return jobDAO.findByEmployerId(employerId);
    }

    public List<Job> search(String keyword, String location, String jobType) {
        return jobDAO.search(keyword, location, jobType);
    }

    public List<Job> advancedSearch(String keyword, String location, String jobType, java.math.BigDecimal minSalary, java.math.BigDecimal maxSalary) {
        return jobDAO.advancedSearch(keyword, location, jobType, minSalary, maxSalary);
    }

    public List<Job> findRecentJobs() {
        return jobDAO.findRecentJobs();
    }

    public List<Job> findLongTermJobs() {
        return jobDAO.findLongTermJobs();
    }

    public boolean update(Job job) throws ValidationException {
        if (job.getTitle() == null || job.getTitle().trim().isEmpty()) {
            throw new ValidationException("Job title is required.");
        }
        return jobDAO.update(job);
    }

    public boolean delete(int id) {
        return jobDAO.delete(id);
    }

    public int countAll() {
        return jobDAO.countAll();
    }

    public int countByEmployer(int employerId) {
        return jobDAO.countByEmployer(employerId);
    }
}
