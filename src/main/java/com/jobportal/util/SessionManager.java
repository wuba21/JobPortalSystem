package com.jobportal.util;

import com.jobportal.model.User;

public class SessionManager {

    private static User currentUser;
    private static int employerId = -1;
    private static com.jobportal.model.Job currentJob;
    private static Integer viewingCompanyId;
    private static String viewingCompanyName;
    private static User contactUser;

    public static User getContactUser() { return contactUser; }
    public static void setContactUser(User user) { contactUser = user; }

    public static void setViewingCompany(Integer id, String name) {
        viewingCompanyId = id;
        viewingCompanyName = name;
    }

    public static Integer getViewingCompanyId() {
        return viewingCompanyId;
    }

    public static String getViewingCompanyName() {
        return viewingCompanyName;
    }

    public static void setCurrentJob(com.jobportal.model.Job job) {
        currentJob = job;
    }

    public static com.jobportal.model.Job getCurrentJob() {
        return currentJob;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void setUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static User getUser() {
        return currentUser;
    }

    public static void setEmployerId(int id) {
        employerId = id;
    }

    public static int getEmployerId() {
        return employerId;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equals(currentUser.getUserType());
    }

    public static boolean isEmployer() {
        return currentUser != null && "EMPLOYER".equals(currentUser.getUserType());
    }

    public static boolean isJobSeeker() {
        return currentUser != null && "JOB_SEEKER".equals(currentUser.getUserType());
    }

    public static void logout() {
        currentUser = null;
        employerId = -1;
        viewingCompanyId = null;
        viewingCompanyName = null;
    }
}
