package com.jobportal.controller;

import com.jobportal.MainApp;
import com.jobportal.model.Job;
import com.jobportal.model.SavedJob;
import com.jobportal.service.JobService;
import com.jobportal.service.SavedJobService;
import com.jobportal.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class SavedJobsController {

    @FXML private VBox jobsContainer;
    @FXML private Label emptyLabel;

    private final SavedJobService savedJobService = new SavedJobService();
    private final JobService jobService = new JobService();

    @FXML
    public void initialize() {
        loadSavedJobs();
    }

    private void loadSavedJobs() {
        jobsContainer.getChildren().clear();
        
        if (SessionManager.getCurrentUser() == null) return;
        
        List<Job> savedJobs = savedJobService.getSavedJobs(SessionManager.getCurrentUser().getId());
        
        if (savedJobs.isEmpty()) {
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
            return;
        }
        
        emptyLabel.setVisible(false);
        emptyLabel.setManaged(false);

        for (Job job : savedJobs) {
            jobsContainer.getChildren().add(createJobCard(job));
        }
    }

    private VBox createJobCard(Job job) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        Label titleLbl = new Label(job.getTitle());
        titleLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label companyLbl = new Label("🏢 " + (job.getCompanyName() != null ? job.getCompanyName() : "Unknown Company"));
        companyLbl.setStyle("-fx-text-fill: #64748b;");

        HBox detailsBox = new HBox(15);
        Label locLbl = new Label("📍 " + job.getLocation());
        locLbl.setStyle("-fx-text-fill: #64748b;");
        Label typeLbl = new Label("💼 " + job.getJobType());
        typeLbl.setStyle("-fx-text-fill: #64748b;");
        detailsBox.getChildren().addAll(locLbl, typeLbl);

        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER_LEFT);
        
        Button viewBtn = new Button("View & Apply");
        viewBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
        viewBtn.setOnAction(e -> {
            SessionManager.setCurrentJob(job);
            MainApp.changeScene("job_details.fxml", job.getTitle());
        });

        Button removeBtn = new Button("Remove");
        removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-border-color: #ef4444; -fx-border-radius: 5; -fx-font-weight: bold; -fx-cursor: hand;");
        removeBtn.setOnAction(e -> {
            savedJobService.removeSavedJob(SessionManager.getCurrentUser().getId(), job.getId());
            loadSavedJobs();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        actionsBox.getChildren().addAll(viewBtn, spacer, removeBtn);

        card.getChildren().addAll(titleLbl, companyLbl, detailsBox, actionsBox);
        return card;
    }
}
