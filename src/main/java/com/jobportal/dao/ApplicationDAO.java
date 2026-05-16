package com.jobportal.dao;

import com.jobportal.model.Application;
import java.util.List;

public interface ApplicationDAO {
    boolean create(Application application);
    Application findById(int id);
    List<Application> findByUserId(int userId);
    List<Application> findByJobId(int jobId);
    List<Application> findByEmployerId(int employerId);
    boolean updateStatus(int applicationId, String status);
    boolean delete(int id);
    List<Application> findAll();
    int countAll();
    int countByStatus(String status);
    boolean hasApplied(int userId, int jobId);
}
