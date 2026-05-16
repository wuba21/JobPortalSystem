package com.jobportal.controller;

import com.jobportal.MainApp;
import com.jobportal.util.AlertUtil;
import com.jobportal.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class EmployerDashboardController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        if (!SessionManager.isEmployer()) {
            MainApp.changeScene("login.fxml", "Login");
            return;
        }
        
        // Ensure employer ID is loaded
        if (SessionManager.getEmployerId() <= 0) {
            loadEmployerId();
        }

        // By default load "Manage Jobs" or "Dashboard overview"
        loadPage("/fxml/employer_manage_jobs.fxml");
    }

    private void loadEmployerId() {
        String sql = "SELECT id FROM employers WHERE user_id = ?";
        try (java.sql.Connection conn = com.jobportal.config.DBConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, SessionManager.getCurrentUser().getId());
            java.sql.ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                SessionManager.setEmployerId(rs.getInt("id"));
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Load employer ID error: " + e.getMessage());
        }
    }

    @FXML
    private void handlePostJob() {
        SessionManager.setCurrentJob(null); // Clear any editing job
        loadPage("/fxml/job-form.fxml");
    }

    @FXML
    private void handleManageJobs() {
        loadPage("/fxml/employer_manage_jobs.fxml");
    }

    @FXML
    private void handleViewApplications() {
        loadPage("/fxml/application-list.fxml");
    }

    @FXML
    private void handleLogout() {
        if (AlertUtil.showConfirmation("Logout", "Are you sure you want to logout?")) {
            SessionManager.logout();
            MainApp.changeScene("login.fxml", "Login");
        }
    }

    private void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Failed to load page: " + fxmlPath);
        }
    }
}
