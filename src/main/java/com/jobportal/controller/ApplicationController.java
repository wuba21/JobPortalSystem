package com.jobportal.controller;

import com.jobportal.MainApp;
import com.jobportal.model.Application;
import com.jobportal.service.ApplicationService;
import com.jobportal.service.UserService;
import com.jobportal.util.AlertUtil;
import com.jobportal.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class ApplicationController {

    @FXML
    private TableView<Application> applicationTable;
    @FXML
    private TableColumn<Application, String> jobTitleColumn;
    @FXML
    private TableColumn<Application, String> applicantColumn;
    @FXML
    private TableColumn<Application, String> statusColumn;
    @FXML
    private TableColumn<Application, String> dateColumn;
    @FXML
    private TableColumn<Application, Void> actionColumn;

    private final ApplicationService applicationService = new ApplicationService();
    private final UserService userService = new UserService();
    private final ObservableList<Application> appList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        jobTitleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getJobTitle()));
        applicantColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getApplicantName()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    badge.setStyle(
                            "-fx-padding: 4 10; -fx-background-radius: 15; -fx-font-weight: bold; -fx-font-size: 12px;");
                    switch (item) {
                        case "ACCEPTED":
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #d1fae5; -fx-text-fill: #065f46;");
                            break;
                        case "REJECTED":
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;");
                            break;
                        case "SHORTLISTED":
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af;");
                            break;
                        case "REVIEWED":
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #fef3c7; -fx-text-fill: #92400e;");
                            break;
                        default:
                            badge.setStyle(badge.getStyle() + "-fx-background-color: #f3f4f6; -fx-text-fill: #374151;");
                            break;
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getAppliedAt() != null ? data.getValue().getAppliedAt().toString() : ""));

        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button resumeBtn = new Button("Resume");
            private final ComboBox<String> statusBox = new ComboBox<>();
            private final Button updateBtn = new Button("Save");
            private final Button msgBtn = new Button("💬 Message");
            private final javafx.scene.layout.HBox employerBox = new javafx.scene.layout.HBox(5, statusBox, updateBtn,
                    resumeBtn, msgBtn);

            {
                resumeBtn.setStyle(
                        "-fx-background-color: #475569; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                updateBtn.setStyle(
                        "-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                msgBtn.setStyle(
                        "-fx-background-color: #0ea5e9; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                statusBox.getItems().addAll("PENDING", "REVIEWED", "SHORTLISTED", "REJECTED", "ACCEPTED");

                resumeBtn.setOnAction(e -> {
                    Application app = getTableRow().getItem();
                    if (app == null)
                        return;
                    if (app.getResumePath() != null && !app.getResumePath().isEmpty()) {
                        try {
                            java.awt.Desktop.getDesktop().open(new java.io.File(app.getResumePath()));
                        } catch (Exception ex) {
                            com.jobportal.util.ToastUtil.showError(resumeBtn.getScene().getWindow(), "Could not open resume.");
                        }
                    } else {
                        com.jobportal.util.ToastUtil.showError(resumeBtn.getScene().getWindow(), "No resume attached.");
                    }
                });

                msgBtn.setOnAction(e -> {
                    Application app = getTableRow().getItem();
                    if (app == null) return;
                    com.jobportal.model.User applicant = userService.findById(app.getUserId());
                    if (applicant != null) {
                        SessionManager.setContactUser(applicant);
                        // Navigate to messages
                        EmployerDashboardController.getInstance().loadCenterPage("messages.fxml");
                    }
                });

                updateBtn.setOnAction(e -> {
                    Application app = getTableRow().getItem();
                    if (app == null)
                        return;
                    String newStatus = statusBox.getValue();
                    if (newStatus != null && applicationService.updateStatus(app.getId(), newStatus)) {

                        // Send Email Notification
                        com.jobportal.model.User applicant = userService.findById(app.getUserId());
                        if (applicant != null && applicant.getEmail() != null) {
                            com.jobportal.util.EmailUtil.sendNotificationEmail(
                                    applicant.getEmail(),
                                    "Application Status Update: " + newStatus,
                                    "Dear " + applicant.getFullName() + ",\n\nYour application for the position of '"
                                            + app.getJobTitle() + "' has been marked as: " + newStatus
                                            + ".\n\nThank you,\nJob Portal Team");
                        }

                        // Create In-App Notification
                        if (applicant != null) {
                            com.jobportal.service.NotificationService notifService = new com.jobportal.service.NotificationService();
                            if ("ACCEPTED".equals(newStatus)) {
                                notifService.createNotification(applicant.getId(), "Your application for the position of '" + app.getJobTitle() + "' was accepted. 🎉");
                            } else if ("REJECTED".equals(newStatus)) {
                                notifService.createNotification(applicant.getId(), "Your application for the position of '" + app.getJobTitle() + "' was rejected. ❌");
                            } else {
                                notifService.createNotification(applicant.getId(), "Your application for the position of '" + app.getJobTitle() + "' is now: " + newStatus);
                            }
                        }

                        com.jobportal.util.ToastUtil.showSuccess(updateBtn.getScene().getWindow(),
                                "Status updated to " + newStatus + ".\n(Notification sent to applicant)");
                        loadApplications();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null) {
                    setGraphic(null);
                } else {
                    Application app = getTableRow().getItem();
                    if (app == null) {
                        setGraphic(null);
                        return;
                    }
                    if (SessionManager.isEmployer() || SessionManager.isAdmin()) {
                        statusBox.setValue(app.getStatus());
                        resumeBtn.setText("CV");
                        setGraphic(employerBox);
                    } else {
                        resumeBtn.setText("View My Resume");
                        setGraphic(resumeBtn);
                    }
                }
            }
        });

        loadApplications();
    }

    private void loadApplications() {
        if (!SessionManager.isLoggedIn() || SessionManager.getCurrentUser() == null) {
            appList.setAll();
            applicationTable.setItems(appList);
            return;
        }

        if (SessionManager.isEmployer()) {
            userService.getOrLoadEmployerId(SessionManager.getCurrentUser());
        }

        List<Application> applications;
        if (SessionManager.isEmployer() && SessionManager.getEmployerId() > 0) {
            applications = applicationService.findByEmployerId(SessionManager.getEmployerId());
        } else if (SessionManager.isAdmin()) {
            applications = applicationService.findAll();
        } else {
            applications = applicationService.findByUserId(SessionManager.getCurrentUser().getId());
        }
        appList.setAll(applications);
        applicationTable.setItems(appList);
    }

    @FXML
    private void handleBackToDashboard() {
        if (SessionManager.getCurrentUser() != null) {
            MainApp.goBackToDashboard();
        } else {
            MainApp.changeScene("Home.fxml", "Home");
        }
    }

    @FXML
    private void handleExportCSV() {
        if (appList.isEmpty()) {
            AlertUtil.showWarning("No Data", "There are no applications to export.");
            return;
        }

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Export Applications to CSV");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("applications.csv");

        java.io.File file = fileChooser.showSaveDialog(applicationTable.getScene().getWindow());
        if (file != null) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                writer.println("Job Title,Applicant Name,Status,Date Applied");
                for (Application app : appList) {
                    writer.printf("\"%s\",\"%s\",\"%s\",\"%s\"\n",
                            app.getJobTitle() != null ? app.getJobTitle().replace("\"", "\"\"") : "",
                            app.getApplicantName() != null ? app.getApplicantName().replace("\"", "\"\"") : "",
                            app.getStatus() != null ? app.getStatus().replace("\"", "\"\"") : "",
                            app.getAppliedAt() != null ? app.getAppliedAt().toString() : "");
                }
                AlertUtil.showInfo("Export Successful", "Data exported successfully to " + file.getName());
            } catch (Exception ex) {
                ex.printStackTrace();
                AlertUtil.showError("Export Failed", "Could not save the CSV file.");
            }
        }
    }
}
