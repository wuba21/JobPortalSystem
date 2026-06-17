package com.jobportal.dao;

import com.jobportal.model.Job;
import java.util.List;

public interface JobDAO {
    boolean create(Job job);
    Job findById(int id);
    List<Job> findAll();
    List<Job> findByEmployerId(int employerId);
    List<Job> search(String keyword, String location, String jobType);
    List<Job> advancedSearch(String keyword, String location, String jobType, java.math.BigDecimal minSalary, java.math.BigDecimal maxSalary);
    List<Job> findRecentJobs();
    List<Job> findLongTermJobs();
    boolean update(Job job);
    boolean delete(int id);
    int countAll();
    int countByEmployer(int employerId);
}
