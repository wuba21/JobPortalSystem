package com.jobportal.controller;

import com.jobportal.MainApp;
import com.jobportal.model.Job;
import com.jobportal.service.JobService;
import com.jobportal.util.AlertUtil;
import com.jobportal.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.List;

public class EmployerManageJobsController {

    @FXML private TableView<Job> jobsTable;
    @FXML private TableColumn<Job, String> titleColumn;
    @FXML private TableColumn<Job, String> locationColumn;
    @FXML private TableColumn<Job, String> deadlineColumn;
    @FXML private TableColumn<Job, Void> actionColumn;
    @FXML private ProgressIndicator loadingSpinner;

    private final JobService jobService = new JobService();
    private ObservableList<Job> jobList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        locationColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLocation()));
        deadlineColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDeadline() != null ? data.getValue().getDeadline().toString() : "No Deadline"));

        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox actionBox = new HBox(10, editBtn, deleteBtn);
            
            {
                editBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 15; -fx-background-radius: 5;");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 15; -fx-background-radius: 5;");
                
                editBtn.setOnAction(e -> {
                    Job job = getTableRow().getItem();
                    if (job != null) {
                        SessionManager.setCurrentJob(job);
                        MainApp.changeScene("job-form.fxml", "Edit Job");
                    }
                });
                
                deleteBtn.setOnAction(e -> {
                    Job job = getTableRow().getItem();
                    if (job != null) {
                        if (AlertUtil.showConfirmation("Delete Job", "Are you sure you want to delete '" + job.getTitle() + "'?")) {
                            if (jobService.delete(job.getId())) {
                                AlertUtil.showInfo("Success", "Job deleted successfully.");
                                loadJobs(); // Reload the list
                            } else {
                                AlertUtil.showError("Error", "Failed to delete job.");
                            }
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(actionBox);
                }
            }
        });

        loadJobs();
    }

    private void loadJobs() {
        if (!SessionManager.isEmployer() || SessionManager.getEmployerId() <= 0) {
            return;
        }

        loadingSpinner.setVisible(true);
        jobsTable.setItems(FXCollections.observableArrayList());

        Task<List<Job>> loadTask = new Task<List<Job>>() {
            @Override
            protected List<Job> call() throws Exception {
                return jobService.findByEmployerId(SessionManager.getEmployerId());
            }
        };

        loadTask.setOnSucceeded(e -> {
            loadingSpinner.setVisible(false);
            jobList.setAll(loadTask.getValue());
            jobsTable.setItems(jobList);
        });

        loadTask.setOnFailed(e -> {
            loadingSpinner.setVisible(false);
            AlertUtil.showError("Error", "Failed to load jobs.");
        });

        Thread bgThread = new Thread(loadTask);
        bgThread.setDaemon(true);
        bgThread.start();
    }

    @FXML
    private void handleBackToDashboard() {
        if (SessionManager.getCurrentUser() != null) {
            if (SessionManager.isEmployer()) {
                MainApp.changeScene("employer_dashboard.fxml", "Employer Dashboard");
            } else {
                MainApp.changeScene("dashboard.fxml", "Dashboard Overview");
            }
        } else {
            MainApp.changeScene("Home.fxml", "Home");
        }
    }
}
