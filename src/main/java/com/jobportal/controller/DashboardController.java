package com.jobportal.controller;

import com.jobportal.MainApp;
import com.jobportal.config.DBConnection;
import com.jobportal.service.ApplicationService;
import com.jobportal.service.JobService;
import com.jobportal.service.UserService;
import com.jobportal.util.AlertUtil;
import com.jobportal.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label userTypeLabel;
    @FXML private Label statsLabel1;
    @FXML private Label statsLabel2;
    @FXML private Label statsLabel3;
    @FXML private VBox employerActions;
    @FXML private VBox adminActions;
    @FXML private Button btnManageJobs;
    @FXML private VBox recommendedSection;
    @FXML private VBox recommendedJobsContainer;

    private final UserService userService = new UserService();
    private final JobService jobService = new JobService();
    private final ApplicationService applicationService = new ApplicationService();

    @FXML
    public void initialize() {
        if (SessionManager.getCurrentUser() == null) {
            MainApp.changeScene("login.fxml", "Login");
            return;
        }

        welcomeLabel.setText("Welcome, " + SessionManager.getCurrentUser().getFullName() + "!");
        userTypeLabel.setText("Role: " + SessionManager.getCurrentUser().getUserType().replace("_", " "));

        // IMMEDIATELY enforce visibility constraints to prevent accidental display if further code throws exceptions
        if (employerActions != null) {
            boolean isEmployerOrAdmin = SessionManager.isEmployer() || SessionManager.isAdmin();
            employerActions.setVisible(isEmployerOrAdmin);
            employerActions.setManaged(isEmployerOrAdmin);
            
            if (btnManageJobs != null) {
                btnManageJobs.setVisible(SessionManager.isEmployer());
                btnManageJobs.setManaged(SessionManager.isEmployer());
            }
        }

        if (adminActions != null) {
            adminActions.setVisible(SessionManager.isAdmin());
            adminActions.setManaged(SessionManager.isAdmin());
        }

        // Load employer ID if employer
        if (SessionManager.isEmployer()) {
            loadEmployerId();
            checkApproachingDeadlines();
        }

        try {
            loadStats();
        } catch (Exception e) {
            System.err.println("Error loading stats: " + e.getMessage());
        }

        if (SessionManager.getCurrentUser() != null && "JOB_SEEKER".equals(SessionManager.getCurrentUser().getUserType())) {
            loadRecommendations();
        }
    }

    private void loadEmployerId() {
        String sql = "SELECT id FROM employers WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, SessionManager.getCurrentUser().getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                SessionManager.setEmployerId(rs.getInt("id"));
            }
        } catch (SQLException e) {
            System.err.println("Load employer ID error: " + e.getMessage());
        }
    }

    private void checkApproachingDeadlines() {
        if (SessionManager.getEmployerId() <= 0) return;
        
        new Thread(() -> {
            String sql = "SELECT id, title, deadline FROM jobs WHERE employer_id = ? AND is_active = TRUE AND deadline IS NOT NULL AND deadline BETWEEN CURRENT_DATE AND DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, SessionManager.getEmployerId());
                ResultSet rs = stmt.executeQuery();
                
                com.jobportal.service.NotificationService notifService = new com.jobportal.service.NotificationService();
                int userId = SessionManager.getCurrentUser().getId();
                
                while (rs.next()) {
                    String title = rs.getString("title");
                    String msg = "⚠️ Deadline approaching soon for your job: '" + title + "'";
                    
                    // Check if already notified to avoid spamming
                    String checkSql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND message = ?";
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                        checkStmt.setInt(1, userId);
                        checkStmt.setString(2, msg);
                        ResultSet crs = checkStmt.executeQuery();
                        if (crs.next() && crs.getInt(1) == 0) {
                            notifService.createNotification(userId, msg);
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("Deadline check error: " + e.getMessage());
            }
        }).start();
    }


    private void loadStats() {
        if (SessionManager.isAdmin()) {
            statsLabel1.setText("Total Users: " + userService.countByType("JOB_SEEKER"));
            statsLabel2.setText("Total Jobs: " + jobService.countAll());
            statsLabel3.setText("Total Applications: " + applicationService.countAll());
        } else if (SessionManager.isEmployer() && SessionManager.getEmployerId() > 0) {
            statsLabel1.setText("My Job Posts: " + jobService.countByEmployer(SessionManager.getEmployerId()));
            statsLabel2.setText("Total Applications: " + applicationService.countAll());
            statsLabel3.setText("");
        } else {
            statsLabel1.setText("Available Jobs: " + jobService.countAll());
            statsLabel2.setText("My Applications: " + applicationService.findByUserId(SessionManager.getCurrentUser().getId()).size());
            statsLabel3.setText("");
        }
    }

    @FXML
    private void handleViewJobs() {
        MainApp.changeScene("job_dashboard.fxml", "Jobs");
    }

    @FXML
    private void handleAdminDashboard() {
        MainApp.changeScene("admin_dashboard.fxml", "Admin Dashboard");
    }

    @FXML
    private void handlePostJob() {
        MainApp.changeScene("job-form.fxml", "Post a Job");
    }

    @FXML
    private void handleManageJobs() {
        MainApp.changeScene("employer_dashboard.fxml", "Employer Dashboard");
    }

    @FXML
    private void handleViewApplications() {
        MainApp.changeScene("application-list.fxml", "Applications");
    }

    @FXML
    private void handleViewApplications() {
        MainApp.changeScene("application-list.fxml", "Applications");
    }

    @FXML
    private void handleLogout() {
        if (AlertUtil.showConfirmation("Logout", "Are you sure you want to logout?")) {
            SessionManager.logout();
            MainApp.changeScene("login.fxml", "Login");
        }
    }

    @FXML
    private void handleStat1Click() {
        if (SessionManager.isAdmin()) {
            AlertUtil.showInfo("Users", "User management module coming soon!");
        } else {
            handleViewJobs();
        }
    }

    @FXML
    private void handleStat2Click() {
        if (SessionManager.isAdmin()) {
            handleViewJobs();
        } else {
            handleViewApplications();
        }
    }

    @FXML
    private void handleStat3Click() {
        if (SessionManager.isAdmin()) {
            handleViewApplications();
        }
    }

    private void loadRecommendations() {
        if (recommendedSection == null || recommendedJobsContainer == null) return;
        
        java.util.List<com.jobportal.model.Application> myApps = applicationService.findByUserId(SessionManager.getCurrentUser().getId());
        java.util.List<com.jobportal.model.Job> recommendations = new java.util.ArrayList<>();
        
        if (!myApps.isEmpty()) {
            // Simple AI logic: Find jobs similar to the last applied job (same job type or title keyword)
            com.jobportal.model.Application lastApp = myApps.get(0); // newest first
            com.jobportal.model.Job lastJob = jobService.findById(lastApp.getJobId());
            if (lastJob != null) {
                java.util.List<com.jobportal.model.Job> similarJobs = jobService.search(null, null, lastJob.getJobType());
                for (com.jobportal.model.Job j : similarJobs) {
                    boolean alreadyApplied = myApps.stream().anyMatch(a -> a.getJobId() == j.getId());
                    if (!alreadyApplied && j.getId() != lastJob.getId()) {
                        recommendations.add(j);
                    }
                    if (recommendations.size() >= 3) break; // show top 3 recommendations
                }
            }
        }
        
        // If no smart recommendations found, just show recent jobs
        if (recommendations.isEmpty()) {
            java.util.List<com.jobportal.model.Job> recent = jobService.findRecentJobs();
            for (com.jobportal.model.Job j : recent) {
                boolean alreadyApplied = myApps.stream().anyMatch(a -> a.getJobId() == j.getId());
                if (!alreadyApplied) {
                    recommendations.add(j);
                }
                if (recommendations.size() >= 3) break;
            }
        }
        
        if (!recommendations.isEmpty()) {
            recommendedSection.setVisible(true);
            recommendedSection.setManaged(true);
            recommendedJobsContainer.getChildren().clear();
            
            for (com.jobportal.model.Job job : recommendations) {
                VBox card = new VBox(5);
                card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2); -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-cursor: hand;");
                
                Label titleLbl = new Label(job.getTitle());
                titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1e293b;");
                
                Label companyLbl = new Label(job.getCompanyName() != null ? job.getCompanyName() : "Unknown Company");
                companyLbl.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 13px;");
                
                javafx.scene.layout.HBox detailsBox = new javafx.scene.layout.HBox(15);
                Label typeLbl = new Label("💼 " + job.getJobType());
                typeLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
                Label locLbl = new Label("📍 " + job.getLocation());
                locLbl.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
                detailsBox.getChildren().addAll(typeLbl, locLbl);
                
                card.getChildren().addAll(titleLbl, companyLbl, detailsBox);
                
                card.setOnMouseClicked(e -> {
                    SessionManager.setCurrentJob(job);
                    MainApp.changeScene("job_details.fxml", job.getTitle());
                });
                
                card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f8fafc; -fx-padding: 15; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(59,130,246,0.15), 15, 0, 0, 5); -fx-border-color: #3b82f6; -fx-border-radius: 8; -fx-cursor: hand;"));
                card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2); -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-cursor: hand;"));
                
                recommendedJobsContainer.getChildren().add(card);
            }
        }
    }
}
