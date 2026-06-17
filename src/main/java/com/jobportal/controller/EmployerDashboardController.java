package com.jobportal.controller;

import com.jobportal.MainApp;
import com.jobportal.util.AlertUtil;
import com.jobportal.util.SessionManager;
import com.jobportal.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.BorderPane;

/**
 * Controller class for the standalone premium Employer Dashboard wrapper layout.
 * Manages parent border pane navigation loaders, static references, profile menus, and auth checks.
 */
public class EmployerDashboardController {

    private static EmployerDashboardController instance;

    @FXML private BorderPane employerBorderPane;
    @FXML private MenuButton profileMenu;

    public static EmployerDashboardController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        // 1. Role-based Authentication Check
        if (SessionManager.getCurrentUser() == null) {
            MainApp.restoreMainLayout("login.fxml", "Login");
            return;
        }
        if (!SessionManager.isEmployer()) {
            AlertUtil.showError("Access Denied", "Only logged-in Employer accounts can view this dashboard.");
            MainApp.restoreMainLayout("login.fxml", "Login");
            return;
        }

        instance = this;

        // Ensure employer ID is loaded
        new UserService().getOrLoadEmployerId(SessionManager.getCurrentUser());

        // Set up profile username text
        String userName = SessionManager.getCurrentUser().getFullName();
        if (profileMenu != null) {
            profileMenu.setText("👤 " + userName);
        }

        // By default, load the Employer Overview sub-view inside the center content area
        loadCenterPage("employer_overview.fxml");
    }

    /**
     * Loads a sub-FXML file dynamically inside the center content area of the Employer Dashboard.
     * Keeps the top header and secondary navbar fixed.
     *
     * @param fxmlFile the name of the FXML file under the resources/fxml/ folder
     */
    public void loadCenterPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFile));
            Parent root = loader.load();
            employerBorderPane.setCenter(root);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Navigation Error", "Could not load sub-page: " + fxmlFile);
        }
    }

    public void loadCenterPage(Parent root) {
        employerBorderPane.setCenter(root);
    }

    // ── Primary Navigation Actions (Top Header Links) ──
    @FXML private void handleNavHome()      { MainApp.restoreMainLayout("Home.fxml", "Home"); }
    @FXML private void handleNavCompanies() { MainApp.restoreMainLayout("companies.fxml", "Companies"); }
    @FXML private void handleNavAbout()     { MainApp.restoreMainLayout("about.fxml", "About"); }
    @FXML private void handleNavContact()   { MainApp.restoreMainLayout("contact.fxml", "Contact"); }

    // ── Secondary Navigation Actions (Dashboard Menu Bar loaded dynamically inside center) ──
    @FXML private void handleNavDashboard()     { loadCenterPage("employer_overview.fxml"); }
    @FXML private void handleNavJobs()          { loadCenterPage("job_dashboard.fxml"); }
    @FXML private void handleCVBuilder()        { loadCenterPage("cv_builder.fxml"); }
    @FXML private void handleManageJobs()       { loadCenterPage("employer_manage_jobs.fxml"); }
    @FXML private void handlePostJob()          { SessionManager.setCurrentJob(null); loadCenterPage("job-form.fxml"); }
    @FXML private void handleViewApplications() { loadCenterPage("application-list.fxml"); }

    // ── Dropdown Actions (Profile Dropdown Menu) ──
    @FXML private void handleViewProfile() { loadCenterPage("profile.fxml"); }
    @FXML private void handleNotifications() { loadCenterPage("notifications.fxml"); }
    @FXML private void handleMessages() { loadCenterPage("messages.fxml"); }
    @FXML private void handleSavedJobs() { loadCenterPage("saved_jobs.fxml"); }
    @FXML private void handleSettings()    { loadCenterPage("settings.fxml"); }
    @FXML
    private void handleLogout() {
        if (AlertUtil.showConfirmation("Logout", "Are you sure you want to logout?")) {
            SessionManager.logout();
            MainApp.restoreMainLayout("login.fxml", "Login");
        }
    }
}
