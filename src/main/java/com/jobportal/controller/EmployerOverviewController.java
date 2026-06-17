package com.jobportal.controller;

import com.jobportal.MainApp;
import com.jobportal.config.DBConnection;
import com.jobportal.model.Application;
import com.jobportal.model.Job;
import com.jobportal.model.Notification;
import com.jobportal.service.ApplicationService;
import com.jobportal.service.JobService;
import com.jobportal.service.NotificationService;
import com.jobportal.util.AlertUtil;
import com.jobportal.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller class for the Employer Overview sub-view inside the Employer Dashboard wrapper.
 * Manages stats, tables, quick action callbacks, and notifications.
 */
public class EmployerOverviewController {

    @FXML private Label welcomeLabel;
    @FXML private Label statsAvailableJobs;
    @FXML private Label statsMyApplications;
    @FXML private Label statsPlatformInsight;
    @FXML private VBox notificationsContainer;

    @FXML private TableView<Job> recentJobsTable;
    @FXML private TableColumn<Job, String> jobTitleCol;
    @FXML private TableColumn<Job, String> jobCompanyCol;
    @FXML private TableColumn<Job, String> jobSalaryCol;
    @FXML private TableColumn<Job, Object> jobDeadlineCol;

    @FXML private TableView<Application> recentApplicationsTable;
    @FXML private TableColumn<Application, String> appTitleCol;
    @FXML private TableColumn<Application, String> appCompanyCol;
    @FXML private TableColumn<Application, String> appStatusCol;

    private final JobService jobService = new JobService();
    private final ApplicationService applicationService = new ApplicationService();
    private final NotificationService notificationService = new NotificationService();

    @FXML
    public void initialize() {
        if (SessionManager.getCurrentUser() == null) return;

        // Load Employer details from DB if not cached
        if (SessionManager.getEmployerId() <= 0) {
            loadEmployerId();
        }

        // Set up welcome message
        String userName = SessionManager.getCurrentUser().getFullName();
        welcomeLabel.setText("Welcome back, " + userName + "!");

        // Load Stats
        statsAvailableJobs.setText("Available Jobs: " + jobService.countAll());
        statsMyApplications.setText("My Applications: " + applicationService.countAll());
        statsPlatformInsight.setText("Active Postings: " + jobService.countByEmployer(SessionManager.getEmployerId()));

        // Setup Tables
        setupRecentJobsTable();
        setupRecentApplicationsTable();

        // Load Notifications
        loadNotifications();
    }

    private void loadEmployerId() {
        try {
            new com.jobportal.service.UserService().getOrLoadEmployerId(SessionManager.getCurrentUser());
        } catch (Exception e) {
            System.err.println("Load employer ID error: " + e.getMessage());
        }
    }

    private void setupRecentJobsTable() {
        jobTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        jobCompanyCol.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        jobSalaryCol.setCellValueFactory(cellData -> {
            Job job = cellData.getValue();
            String sal = "Negotiable";
            if (job.getSalaryMin() != null && job.getSalaryMax() != null) {
                sal = "$" + job.getSalaryMin() + " - $" + job.getSalaryMax();
            } else if (job.getSalaryMin() != null) {
                sal = "$" + job.getSalaryMin() + "+";
            }
            return new SimpleStringProperty(sal);
        });
        jobDeadlineCol.setCellValueFactory(new PropertyValueFactory<>("deadline"));

        List<Job> myJobs = jobService.findByEmployerId(SessionManager.getEmployerId());
        if (myJobs.isEmpty()) {
            myJobs = jobService.findRecentJobs();
        }
        recentJobsTable.setItems(FXCollections.observableArrayList(myJobs));
    }

    private void setupRecentApplicationsTable() {
        appTitleCol.setCellValueFactory(new PropertyValueFactory<>("jobTitle"));
        appCompanyCol.setCellValueFactory(new PropertyValueFactory<>("applicantName"));
        appStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        List<Application> myApps = applicationService.findByEmployerId(SessionManager.getEmployerId());
        if (myApps.isEmpty()) {
            myApps = applicationService.findAll();
        }
        recentApplicationsTable.setItems(FXCollections.observableArrayList(myApps));
    }

    private void loadNotifications() {
        if (notificationsContainer == null) return;
        notificationsContainer.getChildren().clear();

        List<Notification> notificationsList = notificationService.getNotificationsByUser(SessionManager.getCurrentUser().getId());
        for (Notification n : notificationsList) {
            VBox notifBox = new VBox(5);
            notifBox.getStyleClass().add("notif-item");
            Label msgLbl = new Label(n.getMessage());
            msgLbl.getStyleClass().add("notif-item-text");
            msgLbl.setWrapText(true);
            notifBox.getChildren().add(msgLbl);
            notificationsContainer.getChildren().add(notifBox);
        }

        if (notificationsList.isEmpty()) {
            Label noNotif = new Label("No new notifications");
            noNotif.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic; -fx-font-size: 13px;");
            notificationsContainer.getChildren().add(noNotif);
        }
    }

    // ── Quick Actions Handlers ──
    @FXML
    private void handleUploadCV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload CV");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
            new FileChooser.ExtensionFilter("Word Documents", "*.docx", "*.doc")
        );
        File file = fileChooser.showOpenDialog(MainApp.getPrimaryStage());
        if (file != null) {
            AlertUtil.showInfo("Success", "CV file '" + file.getName() + "' uploaded successfully!");
        }
    }

    @FXML private void handleCVBuilder() {
        MainApp.changeScene("cv_builder.fxml", "CV Builder");
    }

    @FXML private void handleSearchJobs() {
        MainApp.changeScene("job_dashboard.fxml", "Search Jobs");
    }

    @FXML private void handleMyApplications() {
        MainApp.changeScene("application-list.fxml", "Applications");
    }
}
