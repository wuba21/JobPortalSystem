package com.jobportal.model;

public class Admin extends User {

    private String adminLevel;

    public Admin() {
        setUserType("ADMIN");
    }

    public Admin(String fullName, String email, String password) {
        super(fullName, email, password, "ADMIN");
    }

    public String getAdminLevel() { return adminLevel; }
    public void setAdminLevel(String adminLevel) { this.adminLevel = adminLevel; }
}
